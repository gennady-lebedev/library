package work.unformed.rest.repository.sql

import work.unformed.rest.meta._
import com.typesafe.scalalogging.LazyLogging

object ApiRepository {
  def apply[T <: Product : DBMapping] = new ApiRepository[T](implicitly[DBMapping[T]])
}

class ApiRepository[T <: Product](db: DBMapping[T]) extends LazyLogging {

  def select(query: Query[T]): Seq[T] = {
    val q = BoundQuery(s"SELECT * FROM ${db.table}") ++ buildWhere(query.filter) ++ buildOrderBy(query.sort) ++ buildPage(query.page)
    logger.debug("Select query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q.map(rs => db.parse(rs))
  }

  def count(query: Query[T]): Long = {
    val q = BoundQuery(s"SELECT count(1) total FROM ${db.table}") ++ buildWhere(query.filter)
    logger.debug("Count query generated: {} with bindings {}", q.sql, q.bindings.map(b => b.name + ":" + b.value).mkString(", "))
    q.get(rs => rs.long("total"))
  }

  private def buildWhere(filters: Seq[Filter]): BoundQuery = {
    if(filters.nonEmpty) {
      BoundQuery("WHERE ") ++ filters.map(toSql).reduce(_ and _)
    } else BoundQuery.empty
  }

  private def toSql(f: Filter): BoundQuery = {
    val c = db.fieldsToColumns.getOrElse(f.field, throw new RuntimeException(s"Undefined field in $f"))
    f.condition match {
      case IsNull => BoundQuery(s"$c IS NULL")
      case NotNull => BoundQuery(s"$c IS NOT NULL")
      case Equals(b) => BoundQuery(s"$c = {$c}", c, b)
      case NotEquals(b) => BoundQuery(s"$c <> {$c}", c, b)
      case Greater(b) => BoundQuery(s"$c > {$c}", c, b)
      case GreaterOrEquals(b) => BoundQuery(s"$c >= {$c}", c, b)
      case Lesser(b) => BoundQuery(s"$c < {$c}", c, b)
      case LesserOrEquals(b) => BoundQuery(s"$c <= {$c}", c, b)
      case Between(a, b) =>
        BoundQuery(s"$c BETWEEN {${c + "_from"}} AND {${c + "_to"}}", Binding(c + "_from", a), Binding(c + "_to", b))
      case In(list) =>
        val indexed = list.zipWithIndex.map { l => Binding(c + "_" + l._2, l._1)}
        BoundQuery(s"$c IN (${indexed.map("{" + _.name + "}").mkString(", ")})", indexed :_*)
    }
  }

  private def buildOrderBy(sort: Seq[Sort]): BoundQuery = {
    if(sort.nonEmpty)
      BoundQuery("ORDER BY " + sort.map(toSql).mkString(", "))
    else
      BoundQuery.empty
  }

  private def toSql(s: Sort): String = {
    val column = db.fieldsToColumns.getOrElse(s.field, throw new RuntimeException(s"Undefined field in $s"))
    if(s.order == Asc)
      column
    else
      s"$column DESC"
  }

  private def buildPage(page: Page): BoundQuery =
    BoundQuery("LIMIT {limit} OFFSET {offset}", Binding("limit", page.limit), Binding("offset", page.offset))
}

