package work.unformed.rest.meta

import scala.reflect.runtime.universe._

trait MetaSupport[T <: Product] {
  implicit val ttag: TypeTag[T]

  lazy val fields: Seq[TermSymbol] = {
    typeOf[T].members.collect {
      case m: TermSymbol if m.isVal => m
    }.toSeq.reverse
  }

  lazy val annotations: Map[String, List[Annotation]] = {
    typeOf[T].typeSymbol.asClass.primaryConstructor.typeSignature.paramLists.flatMap { p =>
      p.map { pp => (pp.name.toString.trim, pp.annotations)}
    }.toMap
  }

  lazy val fieldTypeMap: Map[String, Field] = {
    fields.map { f =>
      val name = f.name.toString.trim
      val isKey = annotations(name).exists(_.tree.tpe =:= typeOf[Key])
      val isAutoincrement = annotations(name).exists(_.tree.tpe =:= typeOf[Auto])
      if(f.typeSignature.typeSymbol == typeOf[Option[_]].typeSymbol)
        (name, Field(f.info.resultType.typeArgs.head, isKey, false, isAutoincrement))
      else
        (name, Field(f.typeSignature, isKey, true, isAutoincrement))
    }.toMap
  }

  lazy val fieldNames: Seq[String] = fields.map(_.name.toString.trim)

  lazy val keys: Seq[String] = fieldTypeMap.filter(t => t._2.isKey).keys.toSeq
  lazy val auto: Seq[String] = fieldTypeMap.filter(t => t._2.isAuto).keys.toSeq

  lazy val meta: EntityMeta = {
    val f = fieldTypeMap.flatMap {
      case (name, field) => Some(name, field)
      case _ => None
    }
    EntityMeta(typeOf[T].typeSymbol.name.toString.trim, f)
  }
}
