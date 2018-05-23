package work.unformed.audit

import akka.http.scaladsl.model.{StatusCodes, headers}
import akka.http.scaladsl.server.Directive._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Directive1}
import com.typesafe.scalalogging.LazyLogging
import work.unformed.library.model.User

import scala.util.{Failure, Success}

object Directives extends LazyLogging {

  def withLock(action: String, user: User): Directive1[Lock] = Directive { inner =>
    extractUri { uri =>
      extractMethod { method =>
        onComplete(AuditService.registerRequest(action, method, uri, user)) {
          case Success(lock) =>
            respondWithHeader(headers.RawHeader("X-Request-ID", lock.id.toString)) {
              inner(Tuple1(lock))
            }
          case Failure(_) =>
            complete(StatusCodes.Forbidden, "Can't acquire lock for request")
        }

      }
    }
  }
}
