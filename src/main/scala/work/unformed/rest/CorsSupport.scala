package work.unformed.rest

import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.server.Directive0
import ch.megard.akka.http.cors.scaladsl.CorsDirectives
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

trait CorsSupport {

  def cors: Directive0 = CorsDirectives.cors(corsSettings())

  def corsSettings(): CorsSettings = {
    val allowed = List(
      HttpMethods.GET,
      HttpMethods.POST,
      HttpMethods.HEAD,
      HttpMethods.OPTIONS,
      HttpMethods.PUT,
      HttpMethods.DELETE
    )

    CorsSettings.defaultSettings.copy(allowedMethods = allowed)
  }
}

