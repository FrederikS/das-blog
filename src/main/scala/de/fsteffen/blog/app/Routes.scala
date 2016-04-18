package de.fsteffen.blog.app

import akka.http.scaladsl.server.Directives._
import de.fsteffen.blog.api.ElasticBasedPostsApiComponent

trait Routes extends ElasticBasedPostsApiComponent {
  val routes = pathPrefix("v1") {
    postRoutes
  }
}
