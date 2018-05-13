package work.unformed.rest.repository.sql

import work.unformed.rest.meta._
import work.unformed.rest.repository.{InvalidUpdateKey, NothingToUpdate}
import com.typesafe.scalalogging.LazyLogging

object ModifySqlBuilder {
  def apply[R <: Product](db: DBMapping[R]): ModifySqlBuilder[R] = new ModifySqlBuilder[R](db)
}

class ModifySqlBuilder[R <: Product](db: DBMapping[R]) extends LazyLogging {

  def select(id: Long): BoundQuery = {
    val key = db.columns.zipWithIndex.filter(c => db.keyColumn.contains(c._1)).head
    val q = BoundQuery(s"SELECT * FROM ${db.table} WHERE ${key._1}={${key._1}}", key._1, id)
    logger.debug("Select query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }

  def select(item: R): BoundQuery = {
    val key = db.columns.zipWithIndex.filter(c => db.keyColumn.contains(c._1)).head
    val q = BoundQuery(s"SELECT * FROM ${db.table} WHERE ${key._1}={${key._1}}", key._1, item.productElement(key._2))
    logger.debug("Select query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }

  def insert(value: R): BoundQuery = {
    val columns = db.columns.zipWithIndex.filterNot(c => db.meta.auto.contains(c._1))
    val bindings = columns.map("{" + _._1 + "}")

    val q = BoundQuery(s"INSERT INTO ${db.table}") ++ BoundQuery(
      columns.map(_._1).mkString("(", ", ", ")") + " VALUES " + bindings.mkString("(", ", ", ")"),
      columns.map(c => Binding(c._1, value.productElement(c._2)))
    )
    logger.debug("Insert query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }

  def update(oldValue: R, newValue: R): BoundQuery = {
    val key = db.columns.zipWithIndex.filter(c => db.keyColumn.contains(c._1)).head
    if (oldValue.productElement(key._2) != newValue.productElement(key._2))
      throw new InvalidUpdateKey(oldValue, newValue)

    val keyValue = newValue.productElement(key._2)

    val columns = db.columns
      .zipWithIndex
      .filterNot(c => db.keyColumn.contains(c._1))
      .filterNot(c => oldValue.productElement(c._2) == newValue.productElement(c._2))

    if (columns.isEmpty) throw new NothingToUpdate()

    val q = BoundQuery(s"UPDATE ${db.table}") ++ BoundQuery(
      "SET " + columns.map(c => c._1 + "={" + c._1 + "}").mkString(","),
      columns.map(c => Binding(c._1, newValue.productElement(c._2)))
    ) ++ BoundQuery(s" WHERE ${key._1}={${key._1}}", key._1, keyValue)
    logger.debug("Update query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }

  def delete(item: R): BoundQuery = {
    val key = db.columns.zipWithIndex.filter(c => db.keyColumn.contains(c._1)).head
    val q = BoundQuery(s"DELETE FROM ${db.table} WHERE ${key._1} = {key}", "key", item.productElement(key._2))
    logger.debug("Delete query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q
  }
}
