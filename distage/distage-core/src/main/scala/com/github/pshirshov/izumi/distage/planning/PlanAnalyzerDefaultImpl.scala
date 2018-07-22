package com.github.pshirshov.izumi.distage.planning

import com.github.pshirshov.izumi.distage.model.plan.ExecutableOp.ProxyOp.{InitProxy, MakeProxy}
import com.github.pshirshov.izumi.distage.model.plan.ExecutableOp.{CreateSet, ImportDependency, InstantiationOp, WiringOp}
import com.github.pshirshov.izumi.distage.model.plan.PlanTopology.empty
import com.github.pshirshov.izumi.distage.model.plan.{ExecutableOp, PlanTopology, XPlanTopology}
import com.github.pshirshov.izumi.distage.model.planning.PlanAnalyzer
import com.github.pshirshov.izumi.distage.model.references.RefTable
import com.github.pshirshov.izumi.distage.model.reflection.universe.RuntimeDIUniverse._

import scala.collection.mutable


class PlanAnalyzerDefaultImpl extends PlanAnalyzer {
  def topoBuild(ops: Seq[ExecutableOp]): PlanTopology = {
    val out = empty
    ops
      .foreach(topoExtend(out, _))
    out.immutable
  }

  def topoExtend(topology: XPlanTopology, op: ExecutableOp): Unit = {
    topology.register(op.target, requirements(op))
  }

  def computeFwdRefTable(plan: Iterable[ExecutableOp]): RefTable = {
    computeFwdRefTable(
      plan
      , (acc) => (key) => acc.contains(key)
      , _._2.nonEmpty
    )
  }

  def computeFullRefTable(plan: Iterable[ExecutableOp]): RefTable = {
    computeFwdRefTable(
      plan
      , (acc) => (key) => false
      , _ => true
    )
  }

  type RefFilter = Accumulator => DIKey => Boolean
  type PostFilter = ((DIKey, mutable.Set[DIKey])) => Boolean

  def requirements(op: ExecutableOp): Set[DIKey] = {
    op match {
      case w: WiringOp =>
        w.wiring match {
          case r: Wiring.UnaryWiring.Reference =>
            Set(r.key)

          case o =>
            o.associations.map(_.wireWith).toSet
        }

      case c: CreateSet =>
        c.members

      case _: MakeProxy =>
        Set.empty

      case _: ImportDependency =>
        Set.empty

      case i: InitProxy =>
        Set(i.proxy.target) ++ requirements(i.proxy.op)
    }
  }

  private def computeFwdRefTable(plan: Iterable[ExecutableOp], refFilter: RefFilter, postFilter: PostFilter): RefTable = {

    val dependencies = plan.toList.foldLeft(new Accumulator) {
      case (acc, op: InstantiationOp) =>
        acc.getOrElseUpdate(op.target, mutable.Set.empty) ++= requirements(op).filterNot(refFilter(acc))
        acc

      case (acc, op) =>
        acc.getOrElseUpdate(op.target, mutable.Set.empty)
        acc
    }
      .filter(postFilter)
      .mapValues(_.toSet).toMap

    val dependants = reverseReftable(dependencies)
    RefTable(dependencies, dependants)
  }

  private def reverseReftable(dependencies: Map[DIKey, Set[DIKey]]): Map[DIKey, Set[DIKey]] = {
    val dependants = dependencies.foldLeft(new Accumulator with mutable.MultiMap[DIKey, DIKey]) {
      case (acc, (reference, referencee)) =>
        referencee.foreach(acc.addBinding(_, reference))
        acc
    }
    dependants.mapValues(_.toSet).toMap
  }
}
