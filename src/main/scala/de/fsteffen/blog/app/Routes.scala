package de.fsteffen.blog.app

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{HttpOrigin, HttpOriginRange}
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.{CorsDirectives, CorsSettings, HttpHeaderRange}
import com.typesafe.config.ConfigFactory

import scala.collection.immutable.Seq
import de.fsteffen.blog.api.ElasticBasedPostsApiComponent

trait Routes extends ElasticBasedPostsApiComponent with CorsDirectives {
  private val config = ConfigFactory.load()
  private val settings = CorsSettings.defaultSettings.copy(
    allowGenericHttpRequests = false,
    allowedOrigins = HttpOriginRange(HttpOrigin(config.getString("cors.allowed-origin"))),
    allowedMethods = Seq(GET, POST, PUT, DELETE, OPTIONS),
    allowedHeaders = HttpHeaderRange("accept", "content-type")
  )

  val routes = pathPrefix("v1") {
    cors(settings) {
      postRoutes
    }
  }
}
