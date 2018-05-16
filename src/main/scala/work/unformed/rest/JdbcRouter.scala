package work.unformed.rest

import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshallable, ToResponseMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, Route}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.circe.{Decoder, Encoder}
import work.unformed.rest.meta.{Meta, QuerySupport, Result}
import work.unformed.rest.repository.JdbcRepository

class JdbcRouter[T <: Product : Meta : Encoder : Decoder](route: PathMatcher[Unit], repo: JdbcRepository[T])
  extends QuerySupport[T] with CirceSupport with Router {

  val csvMarshaller: ToResponseMarshaller[Result[T]] =
    Marshaller.withOpenCharset(MediaTypes.`text/csv`) { (result, charset) ⇒
      HttpResponse(
        entity = HttpEntity.CloseDelimited(
          ContentType(MediaTypes.`text/csv`, HttpCharsets.`UTF-8`),
          Source.fromIterator { () =>
            result.result.map(asCSV).iterator
          }.map(s => ByteString(s))
        )
      )
    }

  val jsonMarshaller: ToResponseMarshaller[Result[T]] = marshaller[Result[T]]

  implicit val resultMarshaller: ToResponseMarshaller[Result[T]] = Marshaller.oneOf(jsonMarshaller, csvMarshaller)

  private def asCSV(value: T): String = {
    value.productIterator.mkString(";") + "\n"
  }

  lazy val routes: Route = metaRoute ~ collectionRoutes ~ itemRoutes

  private val metaRoute: Route = path(route / "meta") {
    get {
      complete(implicitly[Meta[T]])
    }
  }

  private val collectionRoutes: Route = path(route) {
    get {
      resourceQuery { query =>
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
        complete(StatusCodes.OK, repo.update(item))
      }
    } ~ delete {
      entity(as[T]) { item =>
        complete(StatusCodes.NoContent, repo.delete(item))
      }
    }
  }

  private val csvRoute: Route = path(route) {
    get {
      resourceQuery { query =>
        complete (StatusCodes.OK, repo.find(query))
      }
    }
  }
}
