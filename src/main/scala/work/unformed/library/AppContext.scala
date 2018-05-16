package work.unformed.library

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import work.unformed.library.model.{Author, Item, Publisher}
import work.unformed.rest.repository.JdbcRepository
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import work.unformed.db.JdbcSupport
import work.unformed.rest.{CirceSupport, JdbcRouter}
import work.unformed.rest.meta.{DBMapping, Meta}

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
    new JdbcRouter("authors", new JdbcRepository[Author]),
    new JdbcRouter("publishers", new JdbcRepository[Publisher])
  )

  object MetaContext {
    implicit val itemMeta: Meta[Item] = new Meta[Item]
    implicit val authorMeta: Meta[Author] = new Meta[Author]
    implicit val publisherMeta: Meta[Publisher] = new Meta[Publisher]
  }

  object DBContext {
    implicit val itemDb: DBMapping[Item] = new DBMapping[Item](Some("items"))
    implicit val authorDb: DBMapping[Author] = new DBMapping[Author](Some("authors"))
    implicit val publisherDb: DBMapping[Publisher] = new DBMapping[Publisher](Some("publishers"))
  }
}