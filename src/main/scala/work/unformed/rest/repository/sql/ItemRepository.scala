package work.unformed.rest.repository.sql

import work.unformed.rest.meta._
import work.unformed.rest.repository.{InvalidUpdateKey, NothingToUpdate}
import com.typesafe.scalalogging.LazyLogging

object ItemRepository {
  def apply[T <: Product : DBMapping]: ItemRepository[T] = new ItemRepository[T](implicitly[DBMapping[T]])
}

class ItemRepository[T <: Product](db: DBMapping[T]) extends LazyLogging {

  def select(id: Long): Option[T] = {
    val key = db.columns.zipWithIndex.filter(c => db.keyColumns.contains(c._1)).head
    val q = BoundQuery(s"SELECT * FROM ${db.table} WHERE ${key._1}={${key._1}}", key._1, id)
    logger.debug("Select query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q.single(db.parse)
  }

  def select(item: T): Option[T] = {
    val key = db.columns.zipWithIndex.filter(c => db.keyColumns.contains(c._1)).head
    val q = BoundQuery(s"SELECT * FROM ${db.table} WHERE ${key._1}={${key._1}}", key._1, item.productElement(key._2))
    logger.debug("Select query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q.single(db.parse)
  }

  def insert(value: T): Long = {
    val bindings = db.columns
      .zipWithIndex
      .filterNot(a => db.meta.auto.contains(a._1))
      .map(c => Binding(c._1, value.productElement(c._2)))

    val q = BoundQuery(s"INSERT INTO ${db.table}") ++ BoundQuery (
      bindings.map(_.name).mkString("(", ", ", ")") + " VALUES " + bindings.map("{" + _.name + "}").mkString("(", ", ", ")"),
      bindings :_*
    )
    logger.debug("Insert query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q.insert(value)
  }

  def update(oldValue: T, newValue: T): Unit = {
    if (db.meta.keyValues(oldValue) != db.meta.keyValues(newValue))
      throw new InvalidUpdateKey(oldValue, newValue)

    val changed = db.columns
      .zipWithIndex
      .filterNot(c => db.keyColumns.contains(c._1))
      .filterNot(c => oldValue.productElement(c._2) == newValue.productElement(c._2))

    if (changed.isEmpty) throw new NothingToUpdate()

    val q = BoundQuery(s"UPDATE ${db.table}") ++ BoundQuery(
        "SET " + changed.map(c => c._1 + "={" + c._1 + "}").mkString(","),
        changed.map(c => Binding(c._1, newValue.productElement(c._2))) :_*
      ) ++ whereKeysSql(newValue)
    logger.debug("Update query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q.execute
  }

  def delete(item: T): Unit = {
    val q = BoundQuery(s"DELETE FROM ${db.table}") ++ whereKeysSql(item)
    logger.debug("Delete query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q.execute
  }

  private def whereKeysSql(item: T): BoundQuery = {
    if(db.keyColumns.nonEmpty)
      BoundQuery("WHERE") ++
        db.keyColumns
          .zip(db.meta.keyValues(item))
          .map { case(k, v) => BoundQuery(s"$k = {$k}", k, v)}
          .reduce(_ and _)
    else
      throw new RuntimeException(s"Can't build where with keys because keys are empty in ${db.meta.typeName}")
  }
}
