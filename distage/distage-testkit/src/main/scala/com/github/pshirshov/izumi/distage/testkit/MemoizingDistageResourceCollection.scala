package com.github.pshirshov.izumi.distage.testkit

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.{ConcurrentHashMap, ConcurrentLinkedDeque, ExecutorService, TimeUnit}

import com.github.pshirshov.izumi.distage.model.Locator
import com.github.pshirshov.izumi.distage.model.plan.ExecutableOp
import com.github.pshirshov.izumi.distage.model.references.IdentifiedRef
import com.github.pshirshov.izumi.distage.model.reflection.universe.RuntimeDIUniverse._
import com.github.pshirshov.izumi.distage.roles.launcher.ComponentLifecycle
import com.github.pshirshov.izumi.distage.roles.launcher.exceptions.LifecycleException
import com.github.pshirshov.izumi.distage.roles.roles.RoleComponent
import com.github.pshirshov.izumi.logstage.api.IzLogger
import com.github.pshirshov.izumi.fundamentals.platform.language.Quirks._

import scala.collection.JavaConverters._
import scala.util.{Failure, Try}

/**
  * Dangerous sideeffectful thing. Use only in case you know what you do.
  *
  * Allows you to remember instances produced during your tests by a predicate
  * then reuse these instances in other tests within the same classloader.
  */
abstract class MemoizingDistageResourceCollection extends DistageResourceCollection {

  import MemoizingDistageResourceCollection._

  init() // this way we would start shutdown hook only in case we use memoizer

  // synchronization needed to avoid race between memoized components start
  override def startMemoizedComponents(components: Set[RoleComponent])(implicit logger: IzLogger): Unit = memoizedComponentsLifecycle.synchronized {
    components.foreach{
      component =>
        component.synchronized {
          if (!isMemoizedComponentStarted(component)) {
            logger.info(s"Starting memoized component $component...")
            memoizedComponentsLifecycle.push(ComponentLifecycle.Starting(component))
            component.start()
            memoizedComponentsLifecycle.pop().discard()
            memoizedComponentsLifecycle.push(ComponentLifecycle.Started(component))
          } else {
            logger.info(s"Memoized component already started $component.")
          }
        }
    }
  }

  override def isMemoized(resource: Any): Boolean = {
    memoizedInstances.containsValue(resource)
  }

  override def transformPlanElement(op: ExecutableOp): ExecutableOp = synchronized {
    Option(memoizedInstances.get(op.target)) match {
      case Some(value) =>
        ExecutableOp.WiringOp.ReferenceInstance(op.target, Wiring.UnaryWiring.Instance(op.target.tpe, value), op.origin)
      case None =>
        op
    }
  }

  override def processContext(context: Locator): Unit = {
    context.instances.foreach {
      instanceRef =>
        if (memoize(instanceRef)) {
          memoizedInstances.put(instanceRef.key, instanceRef.value)
        }
    }
  }

  private def isMemoizedComponentStarted(component: RoleComponent): Boolean = {
    memoizedComponentsLifecycle.contains(ComponentLifecycle.Started(component)) ||
      memoizedComponentsLifecycle.contains(ComponentLifecycle.Starting(component))
  }

  def memoize(ref: IdentifiedRef): Boolean
}

object MemoizingDistageResourceCollection {
  /**
    * All the memoized instances available in current classloader.
    *
    * Be VERY careful when accessing this field.
    */
  val memoizedInstances = new ConcurrentHashMap[DIKey, Any]()

  /**
    * Contains all lifecycle states of memoized [[com.github.pshirshov.izumi.distage.roles.roles.RoleComponent]]
    */
  private val memoizedComponentsLifecycle = new ConcurrentLinkedDeque[ComponentLifecycle]()

  private val initialized = new AtomicBoolean(false)

  private def getCloseables: List[AutoCloseable] = {
    memoizedInstances.values().asScala.collect {
      case ac: AutoCloseable => ac
    }.toList.reverse
  }

  private def getExecutors: List[ExecutorService] = {
    memoizedInstances.values().asScala.collect {
      case ec: ExecutorService => ec
    }.toList.reverse
  }

  private def shutdownExecutors(): Unit = {
    val toClose = getExecutors
      .filterNot(es => es.isShutdown || es.isTerminated)

    toClose.foreach { es =>
      es.shutdown()
      if (!es.awaitTermination(1, TimeUnit.SECONDS)) {
        es.shutdownNow()
      }
    }
  }

  private def shutdownCloseables(ignore: Set[RoleComponent]): Unit = {
    val closeables = getCloseables.filter {
      case rc: RoleComponent if ignore.contains(rc) => false
      case _ => true
    }
    closeables.foreach(_.close())
  }

  private def shutdownComponents(): Set[RoleComponent] = {
    val toStop = memoizedComponentsLifecycle.asScala.toList.reverse
    val (stopped, _) = toStop
      .map {
        case ComponentLifecycle.Starting(c) =>
          c -> Failure(new LifecycleException(s"Component hasn't been started properly, skipping: $c"))
        case ComponentLifecycle.Started(c) =>
          c -> Try(c.stop())
      }
      .partition(_._2.isSuccess)
    stopped.map(_._1).toSet
  }

  private val shutdownHook = new Thread(() => {
    val ignore = shutdownComponents()
    shutdownCloseables(ignore)
    shutdownExecutors()
    memoizedInstances.clear()
  }, "distage-testkit-finalizer")

  private def init(): Unit = {
    if (initialized.compareAndSet(false, true)) {
      Runtime.getRuntime.addShutdownHook(shutdownHook)
    }
  }
}