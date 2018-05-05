package work.unformed.library

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import work.unformed.library.model.Item
import work.unformed.rest.repository.JdbcRepository
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.DBSession
import work.unformed.db.JdbcSupport

import scala.concurrent.ExecutionContext

object AppContext extends LazyLogging with JdbcSupport {
  val config: Config = ConfigFactory.defaultApplication()
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val session: DBSession = session(config.getConfig("jdbc"))

  val itemsRepository = new JdbcRepository[Item]

  val router = new RootRouter()

}
