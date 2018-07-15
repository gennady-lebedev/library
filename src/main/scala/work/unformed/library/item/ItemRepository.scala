package work.unformed.library.item

import scalikejdbc.{AutoSession, DBSession}
import work.unformed.meta.{DBMapping, Defaults, Query, Result}
import work.unformed.repository.{JdbcRepository, RepositoryItemNotFound}

class ItemRepository(implicit val itemDB: DBMapping[ItemDB]) {
  private val itemDbRepo = JdbcRepository[ItemDB]

  def find(query: Query[ItemDB])(implicit session: DBSession = AutoSession): Result[ItemDB] =
    itemDbRepo.find(query)

  def get(bookId: Long, itemId: Long)(implicit session: DBSession = AutoSession): ItemDB = {
    val db = itemDbRepo.get(itemId)
    if(db.bookId != bookId) throw new RepositoryItemNotFound(itemId, bookId)
    else db
  }

  def create(bookId: Long, draft: ItemDraft)(implicit session: DBSession = AutoSession): ItemDB =
    itemDbRepo.create(ItemDB(
      Defaults.long,
      bookId,
      Draft,
      draft.holder,
      draft.place,
      draft.dueDate
    ))

  def remove(bookId: Long, itemId: Long)(implicit session: DBSession = AutoSession): Unit = {
    val db = itemDbRepo.get(itemId)
    if(db.bookId != bookId) throw new RepositoryItemNotFound(itemId, bookId)
    itemDbRepo.remove(db)
  }

}
