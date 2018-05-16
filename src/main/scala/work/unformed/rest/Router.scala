package work.unformed.rest

import akka.http.scaladsl.server.Route

trait Router {
  val routes: Route
}
