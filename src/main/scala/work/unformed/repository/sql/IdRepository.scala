package work.unformed.repository.sql

import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.{AutoSession, DBSession}
import work.unformed.meta.DBMapping
import work.unformed.repository.RepositoryItemNotFound

object IdRepository {
  def apply[T <: Product : DBMapping]: IdRepository[T] = new IdRepository[T]
}

class IdRepository[T <: Product : DBMapping] extends LazyLogging {
  private val db = implicitly[DBMapping[T]]

  def findById(id: Any)(implicit session: DBSession = AutoSession): Option[T] = {
    val Seq(column) = db.keyColumns
    val q = BoundQuery(s"SELECT * FROM ${db.table} WHERE $column = {$column}", column, id)
    logger.debug("Select by ID generated: {}", q)
    q.single(db.parse)
  }

  def getById(id: Any)(implicit session: DBSession = AutoSession): T = {
    findById(id) match {
      case Some(found) => found
      case None => throw new RepositoryItemNotFound(id)
    }
  }

  def findByIds(ids: Any*): Option[T] = {
    val keys = db.keyColumns
      .zip(ids)
      .map(i => BoundQuery(s"${i._1} = {${i._1}}", i._1, i._2))
      .reduce(_ and _)
    val q = BoundQuery(s"SELECT * FROM ${db.table} WHERE") ++ keys
    logger.debug("Select by ID generated: {}", q)
    q.single(db.parse)
  }

  def getByIds(ids: Any*)(implicit session: DBSession = AutoSession): T = {
    findByIds(ids) match {
      case Some(found) => found
      case None => throw new RepositoryItemNotFound(ids)
    }
  }
}
