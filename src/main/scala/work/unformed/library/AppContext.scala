package work.unformed.library

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import work.unformed.library.model.Item
import work.unformed.rest.repository.JdbcRepository
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import work.unformed.db.JdbcSupport
import work.unformed.rest.meta.{DBMapping, Meta}

import scala.concurrent.ExecutionContext

object AppContext extends LazyLogging with JdbcSupport {
  val config: Config = ConfigFactory.defaultApplication()
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  session(config.getConfig("jdbc"))

  import MetaContext._
  import DBContext._
  val itemsRepository = new JdbcRepository[Item]

  val router = new RootRouter

  object MetaContext {
    implicit val itemMeta: Meta[Item] = new Meta[Item]
  }

  object DBContext {
    implicit val itemDb: DBMapping[Item] = new DBMapping[Item](Some("items"))
  }

  object Routes {
    val itemRouter = new ItemRouter()
  }
}