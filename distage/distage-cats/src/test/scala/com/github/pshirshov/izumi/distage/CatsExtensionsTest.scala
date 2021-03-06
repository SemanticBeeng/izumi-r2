package com.github.pshirshov.izumi.distage

import cats.Id
import cats.effect._
import com.github.pshirshov.izumi.distage.fixtures.BasicCases._
import com.github.pshirshov.izumi.distage.fixtures.CircularCases._
import com.github.pshirshov.izumi.distage.model.PlannerInput
import com.github.pshirshov.izumi.distage.model.definition.ModuleDef
import distage._
import distage.interop.cats._
import org.scalatest.{GivenWhenThen, WordSpec}

class CatsExtensionsTest extends WordSpec with GivenWhenThen {

  "cats-effect extensions" should {
    "work" in {
      import BasicCase1._
      import CircularCase3._

      val definition1 = PlannerInput(new ModuleDef {
        make[SelfReference]
      })

      val injector = Injector.Standard()
      val plan = injector.plan(definition1)

      Then("imports should be empty")
      val plan1 = plan.resolveImportsF[Id] {
        case _ => throw new RuntimeException()
      }

      assert(plan1 === plan)

      Then("traverse should substitute")
      val testDependencyPlan = injector.plan(
        PlannerInput(new ModuleDef {
          make[TestDependency1]
        })
      )
      val testDependencyOp = testDependencyPlan.steps.last

      val plan2 = injector.finish(testDependencyPlan.traverse[Id] { _ => testDependencyOp })

      assert(plan2.steps === testDependencyPlan.steps)

      Then("resolveImportsF should work")
      val plan3 = plan2.resolveImportsF[Id] {
        case _ => new NotInContext {}
      }

      Then("context is correct")
      val context = injector.produceIO[IO](plan3).unsafeRunSync()

      assert(context.get[TestDependency1].unresolved != null)
      assert(!context.instances.map(_.value).contains(null))
    }
  }

}
