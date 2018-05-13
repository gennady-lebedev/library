package work.unformed.library

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.extras.auto._
import work.unformed.library.AppContext.itemsRepository
import work.unformed.library.model.Item
import work.unformed.rest.JsonUtil._
import work.unformed.rest.meta.{Meta, QuerySupport}

class ItemRouter(implicit meta: Meta[Item]) extends QuerySupport[Item] {
  lazy val routes: Route = metaRoute ~ collectionRoutes ~ itemRoutes

  private val metaRoute: Route = path("items" / "meta") {
    get {
      complete(meta)
    }
  }

  private val collectionRoutes: Route = path("items") {
    get {
      resourceQuery { query =>
        complete (StatusCodes.OK, itemsRepository.find(query))
      }
    } ~ post {
      entity(as[Item]) { draft =>
        complete(StatusCodes.Created, itemsRepository.create(draft))
      }
    }
  }

  private val itemRoutes: Route = path("items" / LongNumber) { id =>
    get {
      complete(StatusCodes.OK, itemsRepository.get(id))
    } ~ put {
      entity(as[Item]) { item =>
        complete(StatusCodes.OK, itemsRepository.update(item))
      }
    } ~ delete {
      entity(as[Item]) { item =>
        complete(StatusCodes.NoContent, itemsRepository.delete(item))
      }
    }
  }
}
