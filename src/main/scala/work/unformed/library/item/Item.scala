package work.unformed.library.item

import java.sql.{Date, Timestamp}

import work.unformed.library.model.{Book, User}
import work.unformed.meta.{Auto, Defaults, Key}

sealed trait ItemStatus {
  def allowed(that: ItemStatus): Boolean = (this, that) match {
    case (Draft, OnShelf) => true
    case (Draft, Lost) => true
    case (OnShelf, Draft) => true
    case (OnShelf, OnHands) => true
    case (OnShelf, Lost) => true
    case (OnHands, Returned) => true
    case (OnHands, Lost) => true
    case (Returned, Draft) => true
    case (Returned, OnShelf) => true
    case (Lost, Draft) => true
    case _ => false
  }
}
object ItemStatus {
  def valueOf(s: String): ItemStatus = s match {
    case "Draft" => Draft
    case "OnShelf" => OnShelf
    case "OnHands" => OnHands
    case "Returned" => Returned
    case "Lost" => Lost
  }
}

case object Draft extends ItemStatus
case object OnShelf extends ItemStatus
case object OnHands extends ItemStatus
case object Returned extends ItemStatus
case object Lost extends ItemStatus

case class Item (
  @Key @Auto id: Long = Defaults.long,
  book: Book,
  status: ItemStatus,
  holder: String,
  place: String,
  dueDate: Date
) {
  def withStatus(newStatus: ItemStatus): Item = {
    if(this.status.allowed(newStatus))
      this.copy(status = newStatus)
    else
      throw new RuntimeException(s"Status $newStatus incompatible with current $status of $id")
  }
}

case class ItemDB (
  @Key @Auto id: Long = Defaults.long,
  bookId: Long,
  status: ItemStatus,
  holder: String,
  place: String,
  dueDate: Date
)

case class ItemDraft (
  holder: String,
  place: String,
  dueDate: Date
)

case class ItemLog (
  @Key @Auto id: Long = Defaults.long,
  item: Item,
  status: ItemStatus,
  madeBy: User,
  madeWhen: Timestamp
)
