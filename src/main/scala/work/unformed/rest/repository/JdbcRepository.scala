package work.unformed.rest.repository

import work.unformed.rest.meta.{DBMapping, Query, Result}
import work.unformed.rest.repository.sql.{ApiSqlBuilder, ModifySqlBuilder}
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.{AutoSession, DBSession}

class JdbcRepository[T <: Product](implicit db: DBMapping[T]) extends RwRepository[T, Long] with LazyLogging {

  override def find(query: Query[T])(implicit session: DBSession = AutoSession): Result[T] = {
    val b = ApiSqlBuilder(db, query)
    val result = b.select().map(rs => db.parse(rs))
    val total = b.count().single(rs => rs.long("total")).get
    Result(result, query, total)
  }

  override def count(query: Query[T])(implicit session: DBSession = AutoSession): Long = {
    val b = ApiSqlBuilder(db, query)
    b.count().single(_.long("total")).get
  }

  override def findById(id: Long)(implicit session: DBSession = AutoSession): Option[T] =
    ModifySqlBuilder[T](db).select(id).single(db.parse)

  override def get(id: Long)(implicit session: DBSession = AutoSession): T = findById(id) match {
    case Some(v) => v
    case None => throw new RepositoryItemNotFound(id)
  }

  override def create(draft: T)(implicit session: DBSession = AutoSession): T = {
    val id = ModifySqlBuilder[T](db)
      .insert(draft).insert(draft)
    get(id)
  }


  override def update(entity: T)(implicit session: DBSession = AutoSession): T = {
    ModifySqlBuilder[T](db).select(entity).single(db.parse) match {
      case Some(old) if old == entity => throw new NothingToUpdate
      case Some(old) =>
        ModifySqlBuilder[T](db).update(old, entity).execute
        ModifySqlBuilder[T](db).select(entity).single(db.parse).get
      case None => throw new RepositoryItemNotFound(entity)
    }
  }

  override def delete(entity: T)(implicit session: DBSession = AutoSession): Unit = {
    ModifySqlBuilder[T](db).select(entity).single(db.parse) match {
      case Some(old) if old == entity => ModifySqlBuilder[T](db).delete(entity).execute
      case Some(old) => throw new ConflictOnDelete
      case None => throw new RepositoryItemNotFound(entity)
    }
  }
}
