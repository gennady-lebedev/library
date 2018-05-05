package work.unformed.library.model

import java.sql.Timestamp

import work.unformed.rest.meta.{Auto, Key, MetaCompanion, RepositorySupport}

case class Item
(
  @Key @Auto id: Int = -1,
  name: String,
  optionalComment: Option[String],
  @Auto changed: Timestamp
)

object Item extends MetaCompanion[Item] {
  def meta: RepositorySupport[Item] = new RepositorySupport[Item] {
    override def table: String = "items"
    override def columns: Seq[String] = Seq("id", "name", "comment", "changed")
  }
}