package work.unformed.library.model

import work.unformed.meta.{Auto, Defaults, Key}

case class Author (
  @Key @Auto id: Long = Defaults.long,
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

object StubUser extends User("stub", Reader)