package work.unformed.repository

import scalikejdbc.{AutoSession, DBSession}
import work.unformed.meta.{DBMapping, Query, Result}
import work.unformed.repository.sql.{ApiRepository, IdRepository, ItemRepository}

trait Repository[P <: Product] {
  val db: DBMapping[P]
}

trait ReadRepository[P <: Product, K] extends ApiRepository[P] with IdRepository[P] {
  def find(query: Query[P])(implicit session: DBSession = AutoSession): Result[P] = Result(withQuery(query), query, count(query))
  def get(id: K)(implicit session: DBSession = AutoSession): P = getById(id)
}

trait WriteRepository[P <: Product] extends ReadRepository[P, Long] with ItemRepository[P] {
  def create(draft: P)(implicit session: DBSession = AutoSession): P = {
    get(insertAuto(draft))
  }

  def modify(entity: P)(implicit session: DBSession = AutoSession): P = {
    update(entity)
    select(entity) match {
      case Some(updated) => updated
      case None => throw new UpdateFailed(entity)
    }
  }

  def remove(entity: P)(implicit session: DBSession = AutoSession): Unit = {
    select(entity) match {
      case Some(old) if old == entity => delete(entity)
      case Some(old) => throw new ConflictOnDelete
      case None => throw new RepositoryItemNotFound(entity)
    }
  }
}

trait RwRepository[P <: Product] extends ReadRepository[P, Long] with WriteRepository[P]
