package work.unformed.library.routers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, Route}
import work.unformed.rest.meta.{Meta, QuerySupport, Result}
import work.unformed.rest.repository.JdbcRepository
import work.unformed.rest.JsonUtil._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

class JdbcRouter[T <: Product : Meta : Encoder : Decoder](route: PathMatcher[Unit], repo: JdbcRepository[T]) extends QuerySupport[T] with Router {
  implicit val resultEncoder: Encoder[Result[T]] = deriveEncoder[Result[T]]

  lazy val routes: Route = metaRoute ~ collectionRoutes ~ itemRoutes

  private val metaRoute: Route = path(route / "meta") {
    get {
      complete(implicitly[Meta[T]])
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
