package work.unformed.rest.repository.sql

import scalikejdbc.{AutoSession, DBSession, NoExtractor, SQL, SQLBatch, WrappedResultSet}

case class Binding(name: String, value: Any)

case class BoundQuery(sql: String, bindings: Binding*) {
  def ++(that: BoundQuery): BoundQuery = BoundQuery (
    this.sql + (if(this.sql.nonEmpty && that.sql.nonEmpty) " " else "") + that.sql,
    this.bindings ++ that.bindings :_*
  )

  def and(that: BoundQuery): BoundQuery = BoundQuery (
    this.sql + (if(this.sql.nonEmpty && that.sql.nonEmpty) " AND " else "") + that.sql,
    this.bindings ++ that.bindings :_*
  )

  private def bindByName: SQL[Nothing, NoExtractor] = SQL(sql).bindByName(bindings.map(b => (Symbol(b.name), b.value)) :_*)

  def map[T](f: WrappedResultSet => T)(implicit session: DBSession = AutoSession): Seq[T] = bindByName.map(f).list().apply()
  def single[T](f: WrappedResultSet => T)(implicit session: DBSession = AutoSession): Option[T] = bindByName.map(f).single().apply()
  def get[T](f: WrappedResultSet => T)(implicit session: DBSession = AutoSession): T = single(f).get
  def insert[T](draft: T)(implicit session: DBSession = AutoSession): Unit = bindByName.update().apply()
  def insertAuto[T](draft: T)(implicit session: DBSession = AutoSession): Long = bindByName.updateAndReturnGeneratedKey(1).apply()
  def execute(implicit session: DBSession = AutoSession): Unit = bindByName.execute().apply()

  override def toString: String = s"$sql with ${bindings.map(b => b.name + ":" + b.value).mkString(", ")}"
}

object BoundQuery {
  val empty = BoundQuery("")

  def apply(sql: String): BoundQuery = new BoundQuery(sql)
  def apply(sql: String, bindings: Binding*): BoundQuery = new BoundQuery(sql, bindings :_*)
  def apply(sql: String, param: String, value: Any): BoundQuery = new BoundQuery(sql, Binding(param, value))
}

case class BatchQuery(sql: String, bindings: Seq[Seq[Binding]]) {
  def toScalike: SQLBatch = {
    SQL(sql).batchByName(bindings.map(s => s.map(b => (Symbol(b.name), b.value))):_*)
  }
}