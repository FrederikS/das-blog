package de.fsteffen.blog.app

import akka.http.scaladsl.server.Directives._
import de.fsteffen.blog.api.ElasticBasedPostsApiComponent
import de.fsteffen.blog.http.CorsSupport

trait Routes extends ElasticBasedPostsApiComponent with CorsSupport {
  val routes = pathPrefix("v1") {
    corsHandler {
      postRoutes
    }
  }
}
