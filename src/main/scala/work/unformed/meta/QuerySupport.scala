package work.unformed.meta

import java.sql.{Date, Time, Timestamp}

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{as, entity}
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}
import com.typesafe.scalalogging.LazyLogging
import work.unformed.meta.Meta.Field

import scala.reflect.runtime.universe.typeOf

trait QuerySupport extends LazyLogging {

  implicit def queryUnmarshaller[T <: Product : Meta]: FromRequestUnmarshaller[Query[T]] = Unmarshaller.strict { ctx =>
    val params = ctx.uri.query().toMap

    val page = Page(params.get("limit").map(_.toLong).getOrElse(Page.defaultLimit), params.get("offset").map(_.toLong).getOrElse(Page.defaultOffset))
    val filters = parseFilters(ctx.uri.query().toMultiMap)
    val sort = params.get("sort") match {
      case Some(param) => parseSorting(param)
      case _ => Seq.empty[Sort]
    }
    Query(page, filters, sort)
  }

  def resourceQuery[T <: Product : Meta]: Directive1[Query[T]] = entity(as[Query[T]])

  def parseSorting[T <: Product : Meta](param: String): Seq[Sort] = {
    val names = param.split(",")
    names.flatMap { n =>
      val order = if(n.startsWith("!")) Desc else Asc
      val withoutPrefix = if(n.startsWith("!")) n.drop(1) else n
      if(implicitly[Meta[T]].fieldNames.contains(withoutPrefix))
        Some(Sort(withoutPrefix, order))
      else
        None
    }.toSeq
  }

  def parseFilters[T <: Product : Meta](params: Map[String, Seq[String]]): Seq[Filter] = {
    implicitly[Meta[T]].fieldMap.flatMap {
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
