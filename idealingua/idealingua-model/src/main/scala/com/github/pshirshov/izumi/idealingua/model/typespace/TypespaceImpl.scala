package com.github.pshirshov.izumi.idealingua.model.typespace

import com.github.pshirshov.izumi.idealingua.model.common.TypeId._
import com.github.pshirshov.izumi.idealingua.model.common._
import com.github.pshirshov.izumi.idealingua.model.il.ast.typed._


class TypespaceImpl(val domain: DomainDefinition) extends Typespace with TypeResolver {
  protected[typespace] lazy val types: TypeCollection = new TypeCollection(domain)
  protected[typespace] lazy val referenced: Map[DomainId, Typespace] = domain.referenced.mapValues(d => new TypespaceImpl(d))
  private lazy val index: Map[TypeId, TypeDef] = types.index


  override lazy val inheritance: InheritanceQueries = new InheritanceQueriesImpl(this, types)

  override lazy val structure: StructuralQueries = new StructuralQueriesImpl(types, this, inheritance)

  override def implId(id: InterfaceId): DTOId = DTOId(id, types.toDtoName(id))

  def toDtoName(id: TypeId): String = types.toDtoName(id)

  def apply(id: ServiceId): Service = {
    types.services(id)
  }

  def apply(id: TypeId): TypeDef = {
    if (index.contains(id)) {
      index(id)
    } else {
      referenced(domain.id.toDomainId(id)).apply(id)
    }

//    val typeDomain = domain.id.toDomainId(id)
//    if (domain.id == typeDomain) {
//      id match {
//        case o =>
//          index(o)
//      }
//    } else {
//      referenced(typeDomain).apply(id)
//    }
  }

}


