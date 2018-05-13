package work.unformed.library.model

import java.sql.Timestamp
import work.unformed.rest.meta._

case class Item
(
  @Key @Auto id: Long = Defaults.long,
  name: String,
  optionalComment: Option[String],
  @Auto lastTimeChanged: Timestamp = Defaults.timestamp
)