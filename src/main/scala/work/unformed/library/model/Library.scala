package work.unformed.library.model

import java.sql.{Date, Timestamp}

import work.unformed.rest.meta.{Auto, Defaults, Key}

case class Author (
  @Key @Auto id: Long,
  name: String
)

case class Publisher (
  @Key @Auto id: Long = Defaults.long,
  title: String
)

case class Book (
  @Key @Auto id: Long = Defaults.long,
  title: String,
  isbn: String,
  publisher: Publisher,
  authors: Set[Author]
)

case class BookUI (
  @Key @Auto id: Long = Defaults.long,
  title: String,
  isbn: String,
  publisherId: Long,
  authorsId: Set[Long]
)

case class BookDB (
  @Key @Auto id: Long,
  title: String,
  isbn: String,
  publisherId: Long,
)

case class BookAuthorDB (
  @Key bookId: Long,
  @Key authorId: Long
)

sealed trait UserRole
object UserRole {
  def valueOf(s: String): UserRole = s match {
    case "Reader" => Reader
    case "Librarian" => Librarian
    case "Admin" => Admin
  }
}

case object Reader extends UserRole
case object Librarian extends UserRole
case object Admin extends UserRole

case class User (name: String, role: UserRole)
case class UserSecurity(name: String, pass: String, salt: Array[Byte], role: UserRole)

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
  holder: User,
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

case class ItemLog (
  @Key @Auto id: Long = Defaults.long,
  item: Item,
  status: ItemStatus,
  madeBy: User,
  madeWhen: Timestamp
)