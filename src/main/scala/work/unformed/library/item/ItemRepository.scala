package work.unformed.library.item

import work.unformed.meta.{Query, Result}

class ItemRepository {
  def find(query: Query[ItemDB]): Result[ItemDB] = ???

  def get(bookId: Long, itemId: Long): ItemDB = ???

  def create(bookId: Long, draft: ItemDraft): ItemDB = ???

  def modify(bookId: Long, item: ItemDraft): ItemDB = ???

  def remove(bookId: Long, itemId: Long): ItemDB = ???



}
