package work.unformed.repository

import com.typesafe.scalalogging.LazyLogging
import work.unformed.meta.DBMapping

object JdbcRepository {
  def apply[T <: Product : DBMapping]: JdbcRepository[T] = new JdbcRepository[T]
}

class JdbcRepository[T <: Product : DBMapping] extends RwRepository[T] with LazyLogging {
  override val db: DBMapping[T] = implicitly[DBMapping[T]]
}
