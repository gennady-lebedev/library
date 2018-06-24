package work.unformed.library.book

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, Route}
import work.unformed.library.AppContext
import work.unformed.library.book.BookModel.BookDraft
import work.unformed.library.model.Book
import work.unformed.meta.{Meta, Query, QuerySupport}
import work.unformed.rest.{CirceSupport, Router}

class BookRouter(route: PathMatcher[Unit], repo: BookRepository)(implicit bookMeta: Meta[Book])
  extends QuerySupport with CirceSupport with Router {

  override lazy val routes: Route = metaRoute ~ collectionRoutes ~ itemRoutes

  private val metaRoute: Route = path(route / "meta") {
    get {
      complete(AppContext.MetaContext.bookDraftMeta)
    }
  }

  private val collectionRoutes: Route = path(route) {
    get {
      entity(as[Query[Book]]) { query =>
        complete(repo.find(query))
      }
    } ~ post {
      entity(as[BookDraft]) { draft =>
        complete(StatusCodes.Created, repo.create(draft))
      }
    }
  }

  private val itemRoutes: Route = path(route / LongNumber) { id =>
    get {
      complete(StatusCodes.OK, repo.get(id))
    } ~ put {
      entity(as[BookDraft]) { item =>
        complete(StatusCodes.OK, repo.modify(item))
      }
    } ~ delete {
      complete(StatusCodes.NoContent, repo.remove(id))
    }
  }
}
