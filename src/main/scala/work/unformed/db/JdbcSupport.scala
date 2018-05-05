package work.unformed.db

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.{AutoSession, ConnectionPool, DBSession}
import scalikejdbc._

trait JdbcSupport extends LazyLogging {
  def session(config: Config): DBSession = {
    val driver = config.getString("driver")
    val url = config.getString("url")
    val user = config.getString("user")
    val password = config.getString("pass")
    Class.forName(driver)
    ConnectionPool.singleton(url, user, password)
    implicit val session: DBSession = AutoSession
    sql"SELECT 1+1".execute().apply()
    logger.debug("DB Connection started to {}", url)
    session
  }
}
