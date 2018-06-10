package work.unformed.library.book

import work.unformed.meta.{Auto, Defaults, Key}

object BookModel {
  case class BookDraft (
    @Key @Auto id: Long = Defaults.long,
    title: String,
    isbn: String,
    publisherId: Long,
    authorsId: Set[Long]
  )

  case class BookDB (
    @Key @Auto id: Long = Defaults.long,
    title: String,
    isbn: String,
    publisherId: Long,
  )

  case class BookAuthorDB (
    @Key bookId: Long,
    @Key authorId: Long
  )
}
