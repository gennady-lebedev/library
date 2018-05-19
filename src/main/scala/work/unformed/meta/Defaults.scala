package work.unformed.meta

import java.sql.{Date, Time, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime}

object Defaults {
  val long: Long = -1
  val string: String = ""
  val timestamp: Timestamp = Timestamp.valueOf(LocalDateTime.now())
  val date: Date = Date.valueOf(LocalDate.now())
  val time: Time = Time.valueOf(LocalTime.now())
}
