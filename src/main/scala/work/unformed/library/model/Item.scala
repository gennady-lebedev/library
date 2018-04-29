package work.unformed.library.model

import java.sql.Timestamp

import work.unformed.rest.meta.{MetaCompanion, RepositorySupport}

case class Item
(
  id: Int,
  name: String,
  optionalComment: Option[String],
  changed: Timestamp
)

object Item extends MetaCompanion[Item] {
  def meta: RepositorySupport[Item] = new RepositorySupport[Item] {
    //override def parse(implicit rs: ResultSet): Item = new Item('id, 'name, 'comment, 'changed)
    override def keys: Set[String] = Set("id")
    override def table: String = "items"
    override def columns: Seq[String] = Seq("id", "name", "comment", "changed")
  }
}