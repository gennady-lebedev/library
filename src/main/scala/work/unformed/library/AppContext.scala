package work.unformed.library

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import work.unformed.library.model._
import work.unformed.repository.JdbcRepository
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import work.unformed.db.JdbcSupport
import work.unformed.library.book.BookModel.{BookAuthorDB, BookDB, BookDraft}
import work.unformed.library.book.{BookRepository, BookRouter}
import work.unformed.rest.{CirceSupport, JdbcRouter}
import work.unformed.meta.{DBMapping, Meta}

import scala.concurrent.ExecutionContext

object AppContext extends LazyLogging with JdbcSupport with CirceSupport {
  val config: Config = ConfigFactory.defaultApplication()
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  session(config.getConfig("jdbc"))

  import MetaContext._
  import DBContext._

  val router = new RootRouter(
    //new JdbcRouter("items", new JdbcRepository[Item]),
    new JdbcRouter("authors", JdbcRepository[Author]),
    new JdbcRouter("publishers", JdbcRepository[Publisher]),
  new BookRouter("books", new BookRepository)
  )

  object MetaContext {
    implicit val itemMeta: Meta[Item] = new Meta[Item]
    implicit val authorMeta: Meta[Author] = new Meta[Author]
    implicit val publisherMeta: Meta[Publisher] = new Meta[Publisher]
    implicit val bookMeta: Meta[Book] = new Meta[Book]
    implicit val bookDraftMeta: Meta[BookDraft] = new Meta[BookDraft]
    implicit val bookDbMeta: Meta[BookDB] = new Meta[BookDB]
    implicit val bookAuthorDbMeta: Meta[BookAuthorDB] = new Meta[BookAuthorDB]
  }

  object DBContext {
    implicit val itemDb: DBMapping[Item] = DBMapping[Item]("items")
    implicit val authorDb: DBMapping[Author] = DBMapping[Author]("authors")
    implicit val publisherDb: DBMapping[Publisher] = DBMapping[Publisher]("publishers")
    implicit val bookDb: DBMapping[BookDB] = DBMapping[BookDB]("books")
    implicit val bookAuthorDb: DBMapping[BookAuthorDB] = DBMapping[BookAuthorDB]("books_authors")
  }
}