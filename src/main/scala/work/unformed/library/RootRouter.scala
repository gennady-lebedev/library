package work.unformed.library

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.typesafe.scalalogging.LazyLogging
import work.unformed.rest.JsonUtil
import work.unformed.rest.meta.CorsSupport
import work.unformed.rest.repository.RepositoryError

class RootRouter extends LazyLogging with CorsSupport {

  val routes: Route =
    cors {
      handleExceptions(exceptionHandler){
        extractUri { uri =>
          extractMethod { method =>
            logger.debug("{} {}", method.value, uri.toRelative.path)
            ignoreTrailingSlash {
              healthRoute ~ ItemRouter.routes
            }
          }
        }
      }
    }


  import io.circe.generic.extras.auto._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import JsonUtil._

  private def exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: RepositoryError =>
      logger.warn("Repository error #{}: {}", e.code, e.message)
      complete(e.httpCode, e.response)
    case e: Exception =>
      logger.error("Exception: {}", e.getMessage)
      logger.warn("Exception: ", e)
      complete(StatusCodes.InternalServerError, e)
  }

  private def healthRoute: Route = get {
    path("health") {
      complete(StatusCodes.OK)
    }
  }
}
