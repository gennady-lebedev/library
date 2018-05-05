package work.unformed.library

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import work.unformed.library.AppContext.itemsRepository
import work.unformed.library.model.Item
import work.unformed.rest.JsonUtil
import work.unformed.rest.meta.Query
import work.unformed.rest.repository.{ConflictOnDelete, NothingToUpdate, RepositoryItemNotFound}
import io.circe.generic.extras.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import JsonUtil._

object ItemRouter {
  lazy val routes: Route = metaRoute ~ collectionRoutes ~ itemRoutes

  private val metaRoute: Route = path("items" / "meta") {
    get {
      complete(Item.meta.meta)
    }
  }

  private val collectionRoutes: Route = path("items") {
    get {
      complete (StatusCodes.OK, itemsRepository.find(new Query[Item]))
    } ~ post {
      entity(as[Item]) {
        draft => complete(StatusCodes.Created, itemsRepository.create(draft))
      }
    }
  }

  private val itemRoutes: Route = path("item" / LongNumber) { id =>
    get {
      itemsRepository.findById(id) match {
        case Some(found) => complete(StatusCodes.OK, found)
        case None => complete(StatusCodes.NotFound)
      }
    } ~ put {
      entity(as[Item]) { item =>
        itemsRepository.findById(id) match {
          case Some(existing) if existing != item =>
            complete(StatusCodes.OK, itemsRepository.update(item))
          case Some(existing) => throw new NothingToUpdate
          case None => throw new RepositoryItemNotFound(id)
        }
      }
    } ~ delete {
      entity(as[Item]) { item =>
        itemsRepository.findById(id) match {
          case Some(existing) if existing == item =>
            complete(StatusCodes.NoContent, itemsRepository.delete(item))
          case Some(existing) => throw new ConflictOnDelete
          case None => throw new RepositoryItemNotFound(id)
        }
      }
    }
  }
}
