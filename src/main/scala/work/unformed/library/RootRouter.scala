package work.unformed.library

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.typesafe.scalalogging.LazyLogging
import work.unformed.library.routers.Router
import work.unformed.rest.{CorsSupport, JsonUtil}
import work.unformed.rest.repository.RepositoryError

class RootRouter(routers: Router*) extends LazyLogging with CorsSupport {

  val routes: Route =
    cors {
      handleExceptions(exceptionHandler){
        extractUri { uri =>
          extractMethod { method =>
            logger.debug("{} {}{}", method.value, uri.toRelative.path, uri.queryString().map("?" + _).getOrElse(""))
            ignoreTrailingSlash {
              healthRoute ~ routers.map(_.routes).reduce(_ ~ _)
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
