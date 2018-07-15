package work.unformed.repository.sql

import akka.NotUsed
import akka.stream.scaladsl.Source
import work.unformed.meta.{DBMapping, _}
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.{AutoSession, DBSession}
import work.unformed.repository.Repository

object ApiRepository {
  def apply[T <: Product : DBMapping]: ApiRepository[T] = new ApiRepository[T] {override val db: DBMapping[T] = implicitly[DBMapping[T]]}
}

trait ApiRepository[T <: Product] extends Repository[T] with LazyLogging {
  val streamBatchSize = 1000

  def withQuery(query: Query[T])(implicit session: DBSession = AutoSession): Seq[T] = {
    val q = BoundQuery(s"SELECT * FROM ${db.table}") ++ buildWhere(query.filter) ++ buildOrderBy(query.sort) ++ buildPage(query.page)
    logger.debug("Select query generated: {}", q)
    q map db.parse
  }

  def withoutPaging(query: Query[T])(implicit session: DBSession = AutoSession): Seq[T] = {
    val q = BoundQuery(s"SELECT * FROM ${db.table}") ++ buildWhere(query.filter) ++ buildOrderBy(query.sort)
    logger.debug("Select without paging generated: {}", q)
    q map db.parse
  }

  def count(query: Query[T])(implicit session: DBSession = AutoSession): Long = {
    val q = BoundQuery(s"SELECT count(1) total FROM ${db.table}") ++ buildWhere(query.filter)
    logger.debug("Count query generated: {}", q)
    q.get(rs => rs.long("total"))
  }

  def stream(query: Query[T]): Source[Result[T], NotUsed] = {
    val c = count(query)
    val base = BoundQuery(s"SELECT * FROM ${db.table}") ++ buildWhere(query.filter) ++ buildOrderBy(query.sort)
    Source(0 to c.toInt by streamBatchSize).map { i =>
      Result(base ++ buildPage(Page(streamBatchSize, i)) map db.parse, query, c)
    }
  }

  private def buildWhere(filters: Seq[Filter]): BoundQuery = {
    if(filters.nonEmpty)
      BoundQuery("WHERE") ++ filters.map(toSql).reduce(_ and _)
    else
      BoundQuery.empty
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

