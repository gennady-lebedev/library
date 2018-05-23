package work.unformed.audit

import java.sql.Timestamp
import java.util.UUID

import work.unformed.library.model.User
import work.unformed.meta.Defaults

case class Log (
  id: UUID,
  when: Timestamp = Defaults.timestamp,
  user: User,
  message: String
)

object Log {
  def apply(lock: Lock, message: String): Log = Log (
    lock.id,
    Defaults.timestamp,
    lock.user,
    message
  )
}

case class Lock (
  id: UUID = UUID.randomUUID(),
  user: User,
  action: String
)