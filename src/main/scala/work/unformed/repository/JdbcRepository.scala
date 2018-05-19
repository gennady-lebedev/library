package work.unformed.repository

import work.unformed.meta.{DBMapping, Query, Result}
import work.unformed.repository.sql.{ApiRepository, IdRepository, ItemRepository}
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.{AutoSession, DBSession}

object JdbcRepository {
  def apply[T <: Product : DBMapping]: JdbcRepository[T] = new JdbcRepository[T]
}

class JdbcRepository[T <: Product : DBMapping] extends RwRepository[T, Long] with LazyLogging {

  override def find(query: Query[T])(implicit session: DBSession = AutoSession): Result[T] =
    Result(ApiRepository[T].select(query), query, ApiRepository[T].count(query))

  override def count(query: Query[T])(implicit session: DBSession = AutoSession): Long =
    ApiRepository[T].count(query)

  override def findById(id: Long)(implicit session: DBSession = AutoSession): Option[T] =
    IdRepository[T].findById(id)

  override def get(id: Long)(implicit session: DBSession = AutoSession): T =
    IdRepository[T].getById(id)

  override def create(draft: T)(implicit session: DBSession = AutoSession): T =
    get(ItemRepository[T].insertAuto(draft))


  override def update(entity: T)(implicit session: DBSession = AutoSession): T = {
    ItemRepository[T].select(entity) match {
      case Some(old) if old == entity => throw new NothingToUpdate
      case Some(old) =>
        ItemRepository[T].update(old, entity)
        ItemRepository[T].select(entity).get
      case None => throw new RepositoryItemNotFound(entity)
    }
  }

  override def delete(entity: T)(implicit session: DBSession = AutoSession): Unit = {
    ItemRepository[T].select(entity) match {
      case Some(old) if old == entity => ItemRepository[T].delete(entity)
      case Some(old) => throw new ConflictOnDelete
      case None => throw new RepositoryItemNotFound(entity)
    }
  }
}
