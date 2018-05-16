package work.unformed.rest.meta

import java.sql.{Date, Time, Timestamp}

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{as, entity}
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}
import com.typesafe.scalalogging.LazyLogging
import work.unformed.rest.meta.Meta.Field

import scala.reflect.runtime.universe.typeOf

abstract class QuerySupport[T <: Product](implicit meta: Meta[T]) extends LazyLogging {

  implicit def queryUnmarshaller: FromRequestUnmarshaller[Query[T]] = Unmarshaller.strict { ctx =>
    val params = ctx.uri.query().toMap

    val page = Page(params.getOrElse("limit", "100").toInt, params.getOrElse("offset", "0").toInt)
    val filters = parseFilters(ctx.uri.query().toMultiMap)
    val sort = params.get("sort") match {
      case Some(param) => parseSorting(param)
      case _ => Seq.empty[Sort]
    }
    Query(page, filters, sort)
  }

  def resourceQuery: Directive1[Query[T]] = entity(as[Query[T]])

  def parseSorting(param: String): Seq[Sort] = {
    val names = param.split(",")
    names.flatMap { n =>
      val order = if(n.startsWith("!")) Desc else Asc
      val withoutPrefix = if(n.startsWith("!")) n.drop(1) else n
      if(meta.fieldNames.contains(withoutPrefix))
        Some(Sort(withoutPrefix, order))
      else
        None
    }.toSeq
  }

  def parseFilters(params: Map[String, Seq[String]]): Seq[Filter] = {
    meta.fieldMap.flatMap {
      case (name, Field(t, _, _, _)) if params.contains(name) =>
        val values =
          if (t =:= typeOf[String]) params(name)
          else if (t =:= typeOf[Int]) params(name).map(_.toInt)
          else if (t =:= typeOf[Long]) params(name).map(_.toLong)
          else if (t =:= typeOf[Double]) params(name).map(_.toDouble)
          else if (t =:= typeOf[Boolean]) params(name).map(_.toBoolean)
          else if (t =:= typeOf[BigDecimal]) params(name).map(BigDecimal.apply)
          else if (t =:= typeOf[Date]) params(name).map(Date.valueOf)
          else if (t =:= typeOf[Time]) params(name).map(Time.valueOf)
          else if (t =:= typeOf[Timestamp]) params(name).map(Timestamp.valueOf)
          else throw new RuntimeException(s"Unsupported param $name of type $t")
        Some(Filter(name, In(values)))
      case _ => None
    }.toSeq
  }
}
