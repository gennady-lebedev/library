package work.unformed.rest.repository

import scalikejdbc.{AutoSession, DBSession}
import work.unformed.rest.meta.{Query, Result}

trait Repository[P <: Product, K <: AnyVal]

trait ReadRepository[P <: Product, K <: AnyVal] extends Repository[P, K] {
  def find(query: Query[P])(implicit session: DBSession): Result[P]
  def count(query: Query[P])(implicit session: DBSession): Long
  def findById(id: K)(implicit session: DBSession): Option[P]
  def get(id: K)(implicit session: DBSession): P
}

trait WriteRepository[P <: Product, K <: AnyVal] extends Repository[P, K] {
  def create(draft: P)(implicit session: DBSession): P
  def update(entity: P)(implicit session: DBSession): P
  def delete(entity: P)(implicit session: DBSession): Unit
}

trait RwRepository[P <: Product, K <: AnyVal] extends ReadRepository[P, K] with WriteRepository[P, K]
