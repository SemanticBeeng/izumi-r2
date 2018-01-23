package org.bitbucket.pshirshov.izumi.distage

import org.bitbucket.pshirshov.izumi.distage.definition.Id

object Case1 {

  trait TestDependency0 {
    def boom(): Int = 1
  }

  class TestImpl0 extends TestDependency0 {

  }

  trait NotInContext {}

  trait TestDependency1 {
    // TODO: plan API to let user provide importDefs
    def unresolved: NotInContext
  }

  trait TestDependency3 {
    def methodDependency: TestDependency0

    def doSomeMagic(): Int = methodDependency.boom()
  }

  class TestClass
  (
    val fieldArgDependency: TestDependency0
    , argDependency: TestDependency1
  ) {
    val x = argDependency
    val y = fieldArgDependency
  }

  case class TestCaseClass(a1: TestClass, a2: TestDependency3)

  case class TestInstanceBinding(z: String =
                                 """R-r-rollin' down the window, white widow, fuck fame
Forest fire, climbin' higher, real life, it can wait""")

  case class TestCaseClass2(a: TestInstanceBinding)

  trait JustTrait {}

  class Impl0 extends JustTrait

  class Impl1 extends JustTrait

  class Impl2 extends JustTrait

  class Impl3 extends JustTrait

}

object Case1_1 {

  trait TestDependency0 {
    def boom(): Int = 1
  }

  class TestClass
  (
    @Id("named.test.dependency.0") val fieldArgDependency: TestDependency0
    , @Id("named.test") argDependency: TestInstanceBinding
  ) {
    val x = argDependency
    val y = fieldArgDependency
  }

  class TestImpl0 extends TestDependency0 {

  }

  case class TestInstanceBinding(z: String =
                                 """R-r-rollin' down the window, white widow, fuck fame
Forest fire, climbin' higher, real life, it can wait""")

}

object Case2 {

  trait Circular1 {
    def arg: Circular2
  }

  class Circular2(arg: Circular1)

}

object Case3 {

  trait Circular1 {
    def arg: Circular2
  }

  trait Circular2 {
    def arg: Circular3
  }

  trait Circular3 {
    def arg: Circular4

    def arg2: Circular5
  }

  trait Circular4 {
    def arg: Circular1
  }

  trait Circular5 {
    def arg: Circular1

    def arg2: Circular4
  }

}

object Case4 {

  trait Dependency

  class Impl1 extends Dependency

  class Impl2 extends Dependency

}


object Case5 {

  trait Dependency

  class TestClass(b: Dependency)

  class AssistedTestClass(val a: Int, b: Dependency)

  trait Factory {
    def x(): TestClass
  }

  trait OverridingFactory {
    def x(b: Dependency): TestClass
  }

  trait AssistedFactory {
    def x(a: Int): TestClass
  }

}

object Case6 {

  trait Dependency1

  trait Dependency1Sub extends Dependency1

  class TestClass(b: Dependency1)

  class TestClass2(a: TestClass)

}