package work.unformed.rest

import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshallable, ToResponseMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, Route}

import io.circe.{Decoder, Encoder}
import work.unformed.meta.{Meta, Query, QuerySupport, Result}
import work.unformed.repository.JdbcRepository

class JdbcRouter[T <: Product : Meta : Encoder : Decoder](route: PathMatcher[Unit], repo: JdbcRepository[T])
  extends QuerySupport with CirceSupport with CsvSupport with ExcelSupport with Router {

  lazy val routes: Route = metaRoute ~ collectionRoutes ~ itemRoutes

  private val jsonMarshaller: ToResponseMarshaller[Result[T]] = marshaller[Result[T]]
  private val  csvMarshaller: ToResponseMarshaller[Result[T]] = csvMarshaller[T]
  private val  xslMarshaller: ToResponseMarshaller[Result[T]] = excelMarshaller[T]
  private val resultMarshaller = Marshaller.oneOf(jsonMarshaller, csvMarshaller, xslMarshaller)

  private val metaRoute: Route = path(route / "meta") {
    get {
      complete(implicitly[Meta[T]])
    }
  }

  private val collectionRoutes: Route = path(route) {
    get {
      entity(as[Query[T]]) { query =>
        complete(ToResponseMarshallable(repo.find(query))(resultMarshaller))
      }
    } ~ post {
      entity(as[T]) { draft =>
        complete(StatusCodes.Created, repo.create(draft))
      }
    }
  }

  private val itemRoutes: Route = path(route / LongNumber) { id =>
    get {
      complete(StatusCodes.OK, repo.get(id))
    } ~ put {
      entity(as[T]) { item =>
        complete(StatusCodes.OK, repo.modify(item))
      }
    } ~ delete {
      entity(as[T]) { item =>
        complete(StatusCodes.NoContent, repo.remove(item))
      }
    }
  }
}
