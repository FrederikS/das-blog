package de.fsteffen.blog.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import de.fsteffen.blog.post.{Post, PostRepositoryComponent}
import spray.json.{DefaultJsonProtocol, _}

trait PostsApi extends SprayJsonSupport with DefaultJsonProtocol { this: PostRepositoryComponent =>
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val postFormat = jsonFormat5(Post)

  val postRoutes = path("posts") {
    get {
      complete(postRepository.findAll.map(_.toJson))
    }
  }
}