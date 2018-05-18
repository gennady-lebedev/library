package work.unformed.rest.meta

import work.unformed.rest.meta.Meta.Field

import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._

class Meta[T <: Product : TypeTag] {
  val typeValue: Type = typeOf[T]

  val typeName: String = typeValue.typeSymbol.name.toString.trim

  val fields: Seq[TermSymbol] = {
    typeValue.members.collect {
      case m: TermSymbol if m.isVal => m
    }.toSeq.reverse
  }

  val annotations: Map[String, List[Annotation]] = {
    typeValue.typeSymbol.asClass.primaryConstructor.typeSignature.paramLists.flatMap { p =>
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

  def keyValues(instance: T): Seq[Any] = fieldNames.zipWithIndex.filter(keys.contains).map(k => instance.productElement(k._2))

  def construct(args: Seq[Any]): T = {
    currentMirror
      .reflectClass(typeTag[T].tpe.typeSymbol.asClass)
      .reflectConstructor(
        typeTag[T].tpe.members.filter(m => m.isMethod && m.asMethod.isConstructor).iterator.next.asMethod
      )(args:_*).asInstanceOf[T]
  }
}

object Meta {
  case class Field(`type`: Type, isKey: Boolean, isRequired: Boolean, isAuto: Boolean)
}
