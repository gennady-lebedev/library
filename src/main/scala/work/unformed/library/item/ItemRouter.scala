package work.unformed.library.item

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import work.unformed.library.AppContext
import work.unformed.library.book.BookRepository
import work.unformed.meta._
import work.unformed.rest.{CirceSupport, Router}

class ItemRouter(
  bookRoute: PathMatcher1[Long],
  itemRoute: PathMatcher[Unit],
  repo: ItemRepository,
  bookRepo: BookRepository
)(implicit itemMeta: Meta[Item], draftMeta: Meta[ItemDraft], dbMeta: Meta[ItemDB])
  extends QuerySupport with CirceSupport with Router {

  override lazy val routes: Route = metaRoute ~ collectionRoutes ~ itemRoutes

  private val metaRoute: Route = path(bookRoute / "meta") { _ =>
    get { complete(AppContext.MetaContext.itemMeta) }
  } ~ path(itemRoute / "meta") {
    get { complete(AppContext.MetaContext.itemMeta) }
  }

  private val collectionRoutes: Route = path(bookRoute) { bookId =>
    get {
      entity(as[Query[ItemDB]]) { query =>
        val q = query.copy(filter = query.filter ++ Seq(Filter("bookId", Equals(bookId)))).to[ItemDB]
        complete(repo.find(q))
      }
    } ~ post {
      entity(as[ItemDraft]) { draft =>
        complete(StatusCodes.Created, repo.create(bookId, draft))
      }
    }
  } ~ path(itemRoute) {
    get {
      entity(as[Query[ItemDB]]) { query =>
        complete(repo.find(query))
      }
    }
  }

  private val itemRoutes: Route = path(bookRoute / LongNumber) { (bookId, itemId) =>
    get {
      complete(StatusCodes.OK, repo.get(bookId, itemId))
    } ~ delete {
      complete(StatusCodes.NoContent, repo.remove(bookId, itemId))
    }
  } ~ path(bookRoute / LongNumber / "status") { (bookId, itemId) =>
    put {
      complete(StatusCodes.OK)
    }
  }
}
