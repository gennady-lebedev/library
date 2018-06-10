package work.unformed.library.book

import scalikejdbc.{AutoSession, DBSession}
import work.unformed.library.book.BookModel.{BookAuthorDB, BookDB, BookDraft}
import work.unformed.library.model.{Author, Book, Publisher}
import work.unformed.meta.{DBMapping, Meta, Query, Result}
import work.unformed.repository.sql._

class BookRepository(implicit
  bookDb: DBMapping[BookDB],
  authorDb: DBMapping[Author],
  publisherDb: DBMapping[Publisher],
  bookAuthorDb: DBMapping[BookAuthorDB],
  bookMeta: Meta[Book],
  bookDbMeta: Meta[BookDB]) {

  def find(query: Query[Book])(implicit session: DBSession = AutoSession): Result[Book] = {
    val q = query.to[BookDB]
    Result(
      ApiRepository[BookDB].withQuery(q).map(toBook(_)(session)),
      q.to[Book],
      ApiRepository[BookDB].count(q)
    )
  }

  def get(id: Long)(implicit session: DBSession = AutoSession): Book =
    IdRepository[BookDB].getById(id)

  def create(draft: BookDraft)(implicit session: DBSession = AutoSession): Book = {
    val id = ItemRepository[BookDB].insertAuto(draft)
    BatchRepository[BookAuthorDB].add(draft.authorsId.map(authorId => BookAuthorDB(id, IdRepository[Author].getById(authorId).id)))
    IdRepository[BookDB].getById(id)
  }

  def modify(draft: BookDraft)(implicit session: DBSession = AutoSession): Book = {
    val bookDB = IdRepository[BookDB].getById(draft.id)
    if(toBookDb(draft) != bookDB) {
      ItemRepository[BookDB].update(draft)
    }
    val authors = BatchRepository[Author].itemSet(Binding("book_id", draft.id))
    BatchRepository[BookAuthorDB].updateSet(authors.map(a => BookAuthorDB(draft.id, a.id)), Binding("book_id", draft.id))
    Book (
      bookDB.id,
      bookDB.title,
      bookDB.isbn,
      IdRepository[Publisher].getById(bookDB.publisherId),
      authors
    )
  }

  def remove(id: Long)(implicit session: DBSession = AutoSession): Unit = {
    val bookDB = IdRepository[BookDB].getById(id)
    BatchRepository[BookAuthorDB].deleteAll(Binding("book_id", id))
    ItemRepository[BookDB].delete(bookDB)
  }

  private implicit def toBook(bookDb: BookDB)(implicit session: DBSession = AutoSession): Book = Book (
    bookDb.id,
    bookDb.title,
    bookDb.isbn,
    IdRepository[Publisher].getById(bookDb.publisherId),
    BatchRepository[BookAuthorDB].itemSet(Binding("book_id", bookDb.id))
      .map(db => IdRepository[Author].getById(db.authorId))
  )

  private implicit def toBookDb(draft: BookDraft)(implicit session: DBSession = AutoSession): BookDB = BookDB(
    title = draft.title,
    isbn = draft.isbn,
    publisherId = IdRepository[Publisher].getById(draft.publisherId).id
  )
}
