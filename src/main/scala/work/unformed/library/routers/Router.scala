package work.unformed.library.routers

import akka.http.scaladsl.server.Route

trait Router {
  val routes: Route
}
