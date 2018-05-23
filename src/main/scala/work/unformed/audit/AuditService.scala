package work.unformed.audit

import akka.http.scaladsl.model.{HttpMethod, Uri}
import com.typesafe.scalalogging.LazyLogging
import work.unformed.library.model.User

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object AuditService extends LazyLogging {
  def registerRequest(action: String, method: HttpMethod, uri: Uri, user: User): Future[Lock] = Future {
    val lock = Lock(user = user, action = action)
    logger.debug("{} over {} {}{}", lock, method.value, uri.toRelative.path, uri.queryString().map("?" + _).getOrElse(""))
    lock
  }
}
