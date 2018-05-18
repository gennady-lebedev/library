package work.unformed.rest.meta

import java.sql.{Date, Time, Timestamp}

import scalikejdbc.WrappedResultSet
import work.unformed.rest.meta.Meta.Field

import scala.reflect.runtime.universe.typeOf

class DBMapping[T <: Product]
  (customTable: Option[String] = None, customColumns: Seq[String] = Seq.empty)
  (implicit val meta: Meta[T]) {

  val table: String = customTable.getOrElse(meta.typeName)

  val columns: Seq[String] =
    if(customColumns.isEmpty)
      meta.fieldNames.map(camelToSnake)
    else if(customColumns.size == meta.fieldNames.size)
      customColumns
    else
      throw new RuntimeException(s"Custom columns $customColumns doesn't fit entity ${meta.typeName} with fields ${meta.fieldNames}")

  val fieldsToColumns: Map[String, String] = meta.fieldNames.zip(columns).toMap

  val keyColumns: Seq[String] = meta.keys.map(fieldsToColumns)

  def camelToSnake(s: String): String = {
    val draft = "[A-Z]".r.replaceAllIn(s, { m => "_" + m.group(0).toLowerCase() })
    if(draft.startsWith("_"))
      draft.drop(1)
    else
      draft
  }

  def parse(rs: WrappedResultSet): T = {
    val args = meta.fieldNames.map { name =>
      val Field(t, _, required, _) = meta.fieldMap(name)
      val column = fieldsToColumns(name)

      if (t =:= typeOf[String]) if(required) rs.string(column) else  rs.stringOpt(column)
      else if (t =:= typeOf[Int]) if(required) rs.int(column) else  rs.intOpt(column)
      else if (t =:= typeOf[Long]) if(required) rs.long(column) else  rs.longOpt(column)
      else if (t =:= typeOf[Double]) if(required) rs.double(column) else  rs.doubleOpt(column)
      else if (t =:= typeOf[BigDecimal]) if(required) rs.bigDecimal(column) else  rs.bigDecimalOpt(column)
      else if (t =:= typeOf[Date]) if(required) rs.date(column) else  rs.dateOpt(column)
      else if (t =:= typeOf[Time]) if(required) rs.time(column) else  rs.timeOpt(column)
      else if (t =:= typeOf[Timestamp]) if(required) rs.timestamp(column) else  rs.timestampOpt(column)
      else throw new RuntimeException(s"Unsupported type ${meta.typeName} with field $name : $t")
    }

    meta.construct(args)
  }
}