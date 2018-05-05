package work.unformed.library

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.typesafe.scalalogging.LazyLogging
import work.unformed.rest.JsonUtil
import work.unformed.rest.repository.RepositoryError
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

class RootRouter extends JsonUtil with LazyLogging {

  val routes: Route = handleExceptions(exceptionHandler){
    extractUri { uri =>
      extractMethod { method =>
        logger.debug("{} {}", method.value, uri.toRelative.path)
        ignoreTrailingSlash {
          healthRoute ~ ItemRouter.routes
        }
      }
    }
  }

  private def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: RepositoryError => complete(e.httpCode, e.response)
    case e: Exception =>
      logger.error("Exception: ", e)
      complete(StatusCodes.InternalServerError)
  }

  private def healthRoute: Route = get {
    path("health") {
      complete(StatusCodes.OK)
    }
  }
}
