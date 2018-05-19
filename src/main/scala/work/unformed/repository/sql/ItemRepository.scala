package work.unformed.repository.sql

import work.unformed.meta._
import work.unformed.repository.{InvalidUpdateKey, NothingToUpdate, RepositoryItemNotFound}
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.{AutoSession, DBSession}

object ItemRepository {
  def apply[T <: Product : DBMapping]: ItemRepository[T] = new ItemRepository[T]
}

class ItemRepository[T <: Product : DBMapping] extends LazyLogging {
  private val db = implicitly[DBMapping[T]]

  def select(item: T)(implicit session: DBSession = AutoSession): Option[T] = {
    val q = BoundQuery(s"SELECT * FROM ${db.table}") ++ whereKeysSql(item)
    logger.debug("Select from Entity generated: {}", q)
    q.single(db.parse)
  }

  private def insertBindings(value: T): BoundQuery = {
    val bindings = db.columns
      .zipWithIndex
      .filterNot(a => db.meta.auto.contains(a._1))
      .map(c => Binding(c._1, value.productElement(c._2)))

    val q = BoundQuery(s"INSERT INTO ${db.table}") ++ BoundQuery (
      bindings.map(_.name).mkString("(", ", ", ")") + " VALUES " + bindings.map("{" + _.name + "}").mkString("(", ", ", ")"),
      bindings :_*
    )
    logger.debug("Insert query generated: {}", q)
    q
  }

  def insertAuto(value: T)(implicit session: DBSession = AutoSession): Long = {
    insertBindings(value).insertAuto(value)
  }

  def insert(value: T)(implicit session: DBSession = AutoSession): Unit = {
    insertBindings(value).insert(value)
  }

  def update(newValue: T)(implicit session: DBSession = AutoSession): Unit = {
    select(newValue) match {
      case Some(oldValue) if oldValue != newValue =>
        val changed = db.columns
          .zipWithIndex
          .filterNot(c => db.keyColumns.contains(c._1))
          .filterNot(c => oldValue.productElement(c._2) == newValue.productElement(c._2))

        val q = BoundQuery(s"UPDATE ${db.table}") ++ BoundQuery(
          "SET " + changed.map(c => c._1 + "={" + c._1 + "}").mkString(","),
          changed.map(c => Binding(c._1, newValue.productElement(c._2))) :_*
        ) ++ whereKeysSql(newValue)
        logger.debug("Update query generated: {}", q)
        q.execute

      case Some(notChanged) => throw new NothingToUpdate()
      case None => throw new RepositoryItemNotFound(db.meta.keyValues(newValue))
    }
  }

  def delete(item: T)(implicit session: DBSession = AutoSession): Unit = {
    val q = BoundQuery(s"DELETE FROM ${db.table}") ++ whereKeysSql(item)
    logger.debug("Delete query generated: {}", q)
    q.execute
  }

  private def whereKeysSql(item: T): BoundQuery = {
    if(db.keyColumns.nonEmpty)
      BoundQuery.empty.where(
        db.keyColumns
          .zip(db.meta.keyValues(item))
          .map { case(k, v) => Binding(k, v)} :_*
      )
    else
      throw new RuntimeException(s"Can't build where with keys because keys are empty in ${db.meta.typeName}")
  }
}
