package work.unformed.rest.meta

import work.unformed.rest.meta.Meta.Field

import scala.reflect.runtime.universe._

class Meta[T <: Product : TypeTag] {
  val entity: String = typeOf[T].typeSymbol.name.toString.trim

  val fields: Seq[TermSymbol] = {
    typeOf[T].members.collect {
      case m: TermSymbol if m.isVal => m
    }.toSeq.reverse
  }

  val annotations: Map[String, List[Annotation]] = {
    typeOf[T].typeSymbol.asClass.primaryConstructor.typeSignature.paramLists.flatMap { p =>
      p.map { pp => (pp.name.toString.trim, pp.annotations)}
    }.toMap
  }

  val fieldMap: Map[String, Field] = {
    fields.map { f =>
      val name = f.name.toString.trim
      val isKey = annotations(name).exists(_.tree.tpe =:= typeOf[Key])
      val isAuto = annotations(name).exists(_.tree.tpe =:= typeOf[Auto])
      if(f.typeSignature.typeSymbol == typeOf[Option[_]].typeSymbol)
        (name, Field(f.info.resultType.typeArgs.head, isKey, isRequired = false, isAuto = isAuto))
      else
        (name, Field(f.typeSignature, isKey, isRequired = true, isAuto = isAuto))
    }.toMap
  }

  val fieldNames: Seq[String] = fields.map(_.name.toString.trim)

  val keys: Seq[String] = fieldMap.filter(t => t._2.isKey).keys.toSeq
  val auto: Seq[String] = fieldMap.filter(t => t._2.isAuto).keys.toSeq
}

object Meta {
  case class Field(`type`: Type, isKey: Boolean, isRequired: Boolean, isAuto: Boolean)
}
