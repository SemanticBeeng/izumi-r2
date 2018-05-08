package com.github.pshirshov.izumi.distage

import com.github.pshirshov.izumi.distage.Fixtures._
import com.github.pshirshov.izumi.distage.model.Injector
import com.github.pshirshov.izumi.distage.model.definition.Binding.SingletonBinding
import com.github.pshirshov.izumi.distage.model.definition._
import com.github.pshirshov.izumi.distage.model.exceptions.{MissingInstanceException, TraitInitializationFailedException, UnsupportedWiringException, UntranslatablePlanException}
import com.github.pshirshov.izumi.distage.model.plan.ExecutableOp.{ImportDependency, WiringOp}
import com.github.pshirshov.izumi.distage.model.plan.PlanningFailure.UnsolvableConflict
import com.github.pshirshov.izumi.distage.model.reflection.universe.RuntimeDIUniverse
import com.github.pshirshov.izumi.distage.model.reflection.universe.RuntimeDIUniverse.Wiring.UnaryWiring
import org.scalatest.WordSpec

class InjectorTest extends WordSpec {

  def mkInjector(): Injector = Injectors.bootstrap()

  "DI planner" should {

    "maintain correct operation order" in {
      import Case1._
      val definition: ModuleDef = TrivialModuleDef
        .bind[TestClass]
        .bind[TestDependency3]
        .bind[TestDependency0].as[TestImpl0]
        .bind[TestDependency1]
        .bind[TestCaseClass]
        .bind(TestInstanceBinding())

      val injector = mkInjector()
      val plan = injector.plan(definition)
      assert(plan.steps.exists(_.isInstanceOf[ImportDependency]))

      intercept[MissingInstanceException] {
        injector.produce(plan)
      }

      val fixedPlan = plan.flatMap {
        case ImportDependency(key, _) if key == RuntimeDIUniverse.DIKey.get[NotInContext] =>
          Seq(WiringOp.ReferenceInstance(
            key
            , UnaryWiring.Instance(RuntimeDIUniverse.SafeType.get[NotInContext], new NotInContext {})
          )
          )

        case op =>
          Seq(op)
      }
      injector.produce(fixedPlan)
    }

    "support multiple bindings" in {
      import Case1._
      val definition: ModuleDef = new ModuleBuilder {
        set[JustTrait].named("named.empty.set")

        set[JustTrait]
          .element[JustTrait]
          .element(new Impl1)

        set[JustTrait].named("named.set")
          .element(new Impl2())

        set[JustTrait].named("named.set")
          .element[Impl3]
      }

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)

      assert(context.get[Set[JustTrait]].size == 2)
      assert(context.get[Set[JustTrait]]("named.empty.set").isEmpty)
      assert(context.get[Set[JustTrait]]("named.set").size == 2)
    }


    "support named bindings" in {
      import Case1_1._
      val definition: ModuleDef = TrivialModuleDef
        .bind[TestClass]
        .named("named.test.class")
        .bind[TestDependency0].as[TestImpl0Bad]
        .bind[TestDependency0].as[TestImpl0Good]
        .named("named.test.dependency.0")
        .bind(TestInstanceBinding())
        .named("named.test")

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)
      assert(context.get[TestClass]("named.test.class").correctWired())
    }

    "support circular dependencies" in {
      import Case2._

      val definition: ModuleDef = TrivialModuleDef
        .bind[Circular2]
        .bind[Circular1]

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)
      assert(context.get[Circular1] != null)
      assert(context.get[Circular2] != null)
      assert(context.get[Circular2].arg != null)
    }

    "support complex circular dependencies" in {
      import Case3._

      val definition: ModuleDef = TrivialModuleDef
        .bind[Circular3]
        .bind[Circular1]
        .bind[Circular2]
        .bind[Circular5]
        .bind[Circular4]

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)
      val c3 = context.get[Circular3]
      val traitArg = c3.arg
      assert(traitArg != null && traitArg.isInstanceOf[Circular4])
      assert(c3.method == 2L)
      assert(traitArg.testVal == 1)
      assert(context.enumerate.nonEmpty)
    }

    "support more complex circular dependencies" in {
      import Case15._

      val definition: ModuleDef = TrivialModuleDef
        .bind(CustomDep1.empty)
        .bind(customTraitInstance)
        .bind[CustomClass]
        .bind[CustomDep2]
        .bind[CustomApp]

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)
      assert(context.get[CustomApp] != null)
    }

    "support generics" in {
      import Case11._

      val definition = TrivialModuleDef
        .bind[List[Dep]](List(DepA())).named("As")
        .bind[List[Dep]](List(DepB())).named("Bs")
        .bind[List[DepA]](List(DepA(), DepA(), DepA()))
        .bind[TestClass[DepA]]

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)

      assert(context.get[List[Dep]]("As").forall(_.isInstanceOf[DepA]))
      assert(context.get[List[DepA]].forall(_.isInstanceOf[DepA]))
      assert(context.get[List[Dep]]("Bs").forall(_.isInstanceOf[DepB]))
      assert(context.get[TestClass[DepA]].inner == context.get[List[DepA]])
    }

    "support classes with typealiases" in {
      import Case11._

      val definition = TrivialModuleDef
        .bind[DepA]
        .bind[TestClass2[TypeAliasDepA]]

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)

      assert(context.get[TestClass2[TypeAliasDepA]].inner.isInstanceOf[TypeAliasDepA])
    }

    "support traits with typealiases" in {
      import Case11._

      val definition = TrivialModuleDef
        .bind[DepA]
        .bind[TestTrait]

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)

      assert(context.get[TestTrait].dep.isInstanceOf[TypeAliasDepA])
    }

    "support trait initialization" in {
      import Case3._

      val definition: ModuleDef = TrivialModuleDef
        .bind[CircularBad1]
        .bind[CircularBad2]

      val injector = mkInjector()
      val plan = injector.plan(definition)

      val exc = intercept[TraitInitializationFailedException] {
        injector.produce(plan)
      }
      assert(exc.getCause.isInstanceOf[RuntimeException])

    }

    "support trait fields" in {
      val definition: ModuleDef = TrivialModuleDef
        .bind[Case9.ATraitWithAField]

      val injector = mkInjector()
      val plan = injector.plan(definition)

      val context = injector.produce(plan)
      assert(context.get[Case9.ATraitWithAField].field == 1)
    }

    "fail on unbindable" in {
      import Case4._

      val definition: ModuleDef = new ModuleDef {
        override def bindings: Set[Binding] = Set(
          SingletonBinding(RuntimeDIUniverse.DIKey.get[Dependency], ImplDef.TypeImpl(RuntimeDIUniverse.SafeType.get[Long]))
        )
      }

      val injector = mkInjector()
      intercept[UnsupportedWiringException] {
        injector.plan(definition)
      }
      //assert(exc.badSteps.lengthCompare(1) == 0 && exc.badSteps.exists(_.isInstanceOf[UnbindableBinding]))
    }

    "fail on unsolvable conflicts" in {
      import Case4._

      val definition: ModuleDef = TrivialModuleDef
        .bind[Dependency].as[Impl1]
        .bind[Dependency].as[Impl2]

      val injector = mkInjector()
      val exc = intercept[UntranslatablePlanException] {
        injector.plan(definition)
      }
      assert(exc.badSteps.lengthCompare(1) == 0 && exc.badSteps.exists(_.isInstanceOf[UnsolvableConflict]))
    }

    "handle factory injections" in {
      import Case5._

      val definition: ModuleDef = TrivialModuleDef
        .bind[Factory]
        .bind[Dependency]
        .bind[OverridingFactory]
        .bind[AssistedFactory]
        .bind[AbstractFactory]

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)

      val factory = context.get[Factory]
      assert(factory.wiringTargetForDependency != null)
      assert(factory.factoryMethodForDependency() != factory.wiringTargetForDependency)
      assert(factory.x().b.isInstanceOf[Dependency])

      val abstractFactory = context.get[AbstractFactory]
      assert(abstractFactory.x().isInstanceOf[AbstractDependencyImpl])

      val overridingFactory = context.get[OverridingFactory]
      assert(overridingFactory.x(ConcreteDep()).b.isInstanceOf[ConcreteDep])

      val assistedFactory = context.get[AssistedFactory]
      assert(assistedFactory.x(1).a == 1)
      assert(assistedFactory.x(1).b.isInstanceOf[Dependency])
    }

    "handle generic arguments in cglib factory methods" in {
      import Case5._

      val definition: ModuleDef = TrivialModuleDef
        .bind[GenericAssistedFactory]
        .bind[Dependency](ConcreteDep())

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)

      val instantiated = context.get[GenericAssistedFactory]
      val product = instantiated.x(List(SpecialDep()), List(5))
      assert(product.a.forall(_.isSpecial))
      assert(product.b.forall(_ == 5))
      assert(product.c == ConcreteDep())
    }

    "handle named assisted dependencies in cglib factory methods" in {
      import Case5._

      val definition: ModuleDef = new ModuleBuilder {
        bind[NamedAssistedFactory]
        bind[Dependency]
        bind[Dependency].named("special").as(SpecialDep())
        bind[Dependency].named("veryspecial").as(VerySpecialDep())
      }

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)

      assert(!context.get[Dependency].isSpecial)
      assert(context.get[Dependency]("special").isSpecial)
      assert(context.get[Dependency]("veryspecial").isVerySpecial)

      val instantiated = context.get[NamedAssistedFactory]

      assert(instantiated.dep.isVerySpecial)
      assert(instantiated.x(5).b.isSpecial)
    }

    // BasicProvisionerTest
    "instantiate simple class" in {
      import Case1._
      val definition: ModuleDef = TrivialModuleDef
        .bind[TestCaseClass2]
        .bind(new TestInstanceBinding)

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)
      val instantiated = context.get[TestCaseClass2]

      assert(instantiated.a.z.nonEmpty)
    }

    "instantiate provider bindings" in {
      import Case6._

      val definition: ModuleDef = TrivialModuleDef
        .bind[TestClass].provided((a: Dependency1) => new TestClass(null))
        .bind[Dependency1].provided(() => new Dependency1Sub {})

      val injector = mkInjector()
      val plan = injector.plan(definition)

      val context = injector.produce(plan)
      assert(context.parent.exists(_.plan.steps.nonEmpty))
      val instantiated = context.get[TestClass]
      assert(instantiated.b == null)
    }

    "support named bindings in cglib traits" in {
      import Case10._

      val definition = TrivialModuleDef
        .bind[Dep].as[DepA].named("A")
        .bind[Dep].as[DepB].named("B")
        .bind[Trait]
        .bind[Trait1]

      val injector = mkInjector()
      val plan = injector.plan(definition)

      val context = injector.produce(plan)
      val instantiated = context.get[Trait]

      assert(instantiated.depA.isA)
      assert(!instantiated.depB.isA)

      val instantiated1 = context.get[Trait1]

      assert(instantiated1.depA.isA)
      assert(!instantiated1.depB.isA)
    }

    "type annotations in di keys do not result in different keys" in {
      import Case8._

      val definition = TrivialModuleDef
        .bind[Dependency1 @Id("special")]
        .bind[Trait1]

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)

      val instantiated = context.find[Dependency1]
      val instantiated1 = context.find[Dependency1 @Id("special")]
      assert(instantiated1.isDefined)
      assert(instantiated.isDefined)
    }

    "support named bindings in method reference providers" in {
      import Case17._

      val definition = TrivialModuleDef
        .bind[TestDependency].named("classdeftypeann1")
        .bind[TestClass].provided(implType _)

      val injector = mkInjector()
      val plan = injector.plan(definition)
      val context = injector.produce(plan)

      val dependency = context.get[TestDependency]("classdeftypeann1")
      val instantiated = context.get[TestClass]

      assert(instantiated.a == dependency)
    }

    "support named bindings in lambda providers" in {
      import Case17._

      val definition = TrivialModuleDef
        .bind[TestDependency].named("classdeftypeann1")
        .bind[TestClass].provided { t: TestDependency @Id("classdeftypeann1") => new TestClass(t) }

      val injector = mkInjector()
      val context = injector.produce(injector.plan(definition))

      val dependency = context.get[TestDependency]("classdeftypeann1")
      val instantiated = context.get[TestClass]

      assert(instantiated.a == dependency)
    }

    "populates implicit parameters in class constructor from explicit DI context instead of scala's implicit resolution" in {
      import Case13._

      val definition = TrivialModuleDef
        .bind[TestClass]
        .bind[Dep]
        .bind[DummyImplicit].as[MyDummyImplicit]

      val injector = mkInjector()
      val plan = injector.plan(definition)

      val context = injector.produce(plan)
      val instantiated = context.get[TestClass]

      assert(instantiated.dummyImplicit.isInstanceOf[MyDummyImplicit])
      assert(instantiated.dummyImplicit.asInstanceOf[MyDummyImplicit].imADummy)
    }

    "override protected defs in cglib traits" in {
      import Case14._

      val definition = TrivialModuleDef
        .bind[TestTrait]
        .bind[Dep]

      val injector = mkInjector()
      val plan = injector.plan(definition)

      val context = injector.produce(plan)
      val instantiated = context.get[TestTrait]

      assert(instantiated.rd == Dep().toString)
    }

    "handle set bindings ordering" in {
      import Case18._

      val definition = new ModuleBuilder {
        bind[Service2]
        bind[Service0]
        bind[Service1]
        bind[Service3]

        set[SetTrait]
          .element[SetImpl1]
          .element[SetImpl2]
          .element[SetImpl3]

        set[SetTrait].named("n1")
          .element[SetImpl1]
          .element[SetImpl2]
          .element[SetImpl3]

        set[SetTrait].named("n2")
          .element[SetImpl1]
          .element[SetImpl2]
          .element[SetImpl3]

        set[SetTrait].named("n3")
          .element[SetImpl1]
          .element[SetImpl2]
          .element[SetImpl3]
      }

      val injector = mkInjector()
      val plan = injector.plan(definition)

      val context = injector.produce(plan)

      assert(context.get[Service0].set.size == 3)
      assert(context.get[Service1].set.size == 3)
      assert(context.get[Service2].set.size == 3)
      assert(context.get[Service3].set.size == 3)
    }

    "ModuleBuilder supports tags" in {
      import Case18._

      val definition = new ModuleBuilder {
        set[SetTrait].named("n1").tagged("A", "B")
          .element[SetImpl1].tagged("A")
          .element[SetImpl1].tagged("B")
          .element[SetImpl3].tagged("A").tagged("B")

        bind[Service1].tagged("CA").tagged("CB").as[Service1]

        bind[Service1].tagged("CC")

        set[SetTrait].tagged("A", "B")
      }

      assert(definition.bindings.count(_.tags == Set("A", "B")) == 3)
      assert(definition.bindings.count(_.tags == Set("CA", "CB")) == 1)
      assert(definition.bindings.count(_.tags == Set("CC")) == 1)
      assert(definition.bindings.count(_.tags == Set("A")) == 1)
      assert(definition.bindings.count(_.tags == Set("B")) == 1)
    }

  }

}
