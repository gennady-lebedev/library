package work.unformed.rest.meta

import scala.reflect.runtime.universe.Type

case class EntityMeta(entity: String, fields: Map[String, Field])

case class Field(`type`: Type, isKey: Boolean, isRequired: Boolean, isAuto: Boolean)