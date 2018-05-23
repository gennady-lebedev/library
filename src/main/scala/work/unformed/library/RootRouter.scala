package work.unformed.library

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import com.typesafe.scalalogging.LazyLogging
import work.unformed.audit.Directives._
import work.unformed.library.model.StubUser
import work.unformed.rest.{CirceSupport, CorsSupport, Router}
import work.unformed.repository.RepositoryError

class RootRouter(routers: Router*) extends LazyLogging with CorsSupport with CirceSupport {

  val routes: Route =
    cors {
      handleExceptions(exceptionHandler){
        withLock("something", StubUser) { lock =>
          ignoreTrailingSlash {
            healthRoute ~ routers.map(_.routes).reduce(_ ~ _)
          }
        }
      }
    }

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
