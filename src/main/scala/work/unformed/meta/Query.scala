package work.unformed.meta

case class Query[P <: Product](
  page: Page = Page(),
  filter: Seq[Filter] = Seq.empty,
  sort: Seq[Sort] = Seq.empty
) {
  def to[T <: Product : Meta]: Query[T] = Query[T](
    this.page,
    this.filter.filter(f => implicitly[Meta[T]].fieldNames.contains(f.field)),
    this.sort.filter(f => implicitly[Meta[T]].fieldNames.contains(f.field))
  )
}

object Query {
  def default[P <: Product]: Query[P] = Query()
}

case class Page(limit: Int = Page.defaultLimit, offset: Int = Page.defaultOffset)

object Page {
  val defaultLimit = 50
  val defaultOffset = 0
}

case class Filter(field: String, condition: Predicate) {
  override def toString: String = condition match {
    case IsNull => s"$field is null"
    case NotNull => s"$field is not null"
    case Equals(v) => s"$field = $v"
    case NotEquals(v) => s"$field != $v"
    case Greater(v) => s"$field > $v"
    case GreaterOrEquals(v) => s"$field >= $v"
    case Lesser(v) => s"$field < $v"
    case LesserOrEquals(v) => s"$field <= $v"
    case In(in) => s"$field in (${in.mkString(", ")})"
    case Between(from, to) => s"$field between $from and $to"
  }
}

sealed trait Predicate

case object IsNull extends Predicate
case object NotNull extends Predicate
case class Equals(value: Any) extends Predicate
case class NotEquals(value: Any) extends Predicate
case class In(in: Seq[Any]) extends Predicate
case class Greater(value: Any) extends Predicate
case class GreaterOrEquals(value: Any) extends Predicate
case class Lesser(value: Any) extends Predicate
case class LesserOrEquals(value: Any) extends Predicate
case class Between(from: Any, to: Any) extends Predicate

case class Sort(field: String, order: SortOrder)
trait SortOrder
case object Asc extends SortOrder
case object Desc extends SortOrder

case class Result[P <: Product]
(
  result: Seq[P],
  filter: Seq[Filter],
  sort: Seq[Sort],
  count: Long,
  limit: Long,
  offset: Long
)

object Result {
  def apply[P <: Product](result: Seq[P], query: Query[P], total: Long): Result[P] =
    new Result(result, query.filter, query.sort, total, query.page.limit, query.page.offset)

  def apply[P <: Product](result: Seq[P], total: Int): Result[P] =
    new Result(result, Seq.empty, Seq.empty, total, Page.defaultLimit, Page.defaultOffset)
}