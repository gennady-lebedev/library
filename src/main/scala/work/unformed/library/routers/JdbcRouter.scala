package work.unformed.library.routers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, Route}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.extras.auto._
import io.circe.syntax._
import work.unformed.rest.JsonUtil._
import work.unformed.rest.meta.{Meta, QuerySupport}
import work.unformed.rest.repository.JdbcRepository

class JdbcRouter[T <: Product](route: PathMatcher[Unit], repo: JdbcRepository[T])(implicit meta: Meta[T]) extends QuerySupport[T] with Router {
  lazy val routes: Route = metaRoute ~ collectionRoutes ~ itemRoutes

  implicit val en: Encoder[T] = new Encoder[T] {
    override def apply(a: T): Json = a.asJson
  }

  implicit val de: Decoder[T] = new Decoder[T] {
    override def apply(c: HCursor): Result[T] = c.as[T]
  }

  private val metaRoute: Route = path(route / "meta") {
    get {
      complete(meta)
    }
  }

  private val collectionRoutes: Route = path(route) {
    get {
      resourceQuery { query =>
        complete (StatusCodes.OK, repo.find(query))
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
}
