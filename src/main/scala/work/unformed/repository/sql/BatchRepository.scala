package work.unformed.repository.sql

import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.{AutoSession, DBSession}
import work.unformed.meta.DBMapping

object BatchRepository {
  def apply[T <: Product : DBMapping]: BatchRepository[T] = new BatchRepository[T]
}

class BatchRepository[T <: Product : DBMapping] extends LazyLogging {
  private val db = implicitly[DBMapping[T]]

  def itemSeq(bindings: Binding*)(implicit session: DBSession = AutoSession): Seq[T] =
    BoundQuery(s"SELECT * FROM ${db.table}")
      .where(bindings:_*)
      .map(db.parse)
      .toSeq

  def itemSet(bindings: Binding*)(implicit session: DBSession = AutoSession): Set[T] =
    BoundQuery(s"SELECT * FROM ${db.table}")
      .where(bindings:_*)
      .map(db.parse)
      .toSet

  //TODO refactor
  def add(items: Iterable[T])(implicit session: DBSession = AutoSession): Unit = {
    def bindings: Seq[Binding] =
      db.columns
        .zipWithIndex
        .filterNot(a => db.meta.auto.contains(a._1))
        .map(c => Binding(c._1, c._2))

    val q = BoundQuery(s"INSERT INTO ${db.table}") ++ BoundQuery (
      bindings.map(_.name).mkString("(", ", ", ")") + " VALUES " + bindings.map("{" + _.name + "}").mkString("(", ", ", ")"))

    q
      .toBatch(items.map(i => bindings.map(b => Binding(b.name, i.productElement(b.value.asInstanceOf[Int])))).toSeq)
      .toScalike
      .apply()
  }

  def updateSeq(newValues: Seq[T], bindings: Binding*)(implicit session: DBSession = AutoSession): Seq[T] = {
    delete(bindings:_*)
    add(newValues)
    itemSeq(bindings:_*)
  }

  def updateSet(newValues: Set[T], bindings: Binding*)(implicit session: DBSession = AutoSession): Set[T] = {
    val oldValues = itemSet(bindings:_*)
    db.meta.reconcile(newValues, oldValues)
        .doOnCreated(add)
        .doOnRemoved(_.foreach(ItemRepository[T].delete))
        .doOnUpdated(_.foreach(ItemRepository[T].updateUnsafe))
    itemSet(bindings:_*)
  }

  def delete(bindings: Binding*)(implicit session: DBSession = AutoSession): Unit =
    BoundQuery(s"DELETE ${db.table}")
      .where(bindings:_*)
      .execute

}
