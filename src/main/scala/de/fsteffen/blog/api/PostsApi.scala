package de.fsteffen.blog.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import de.fsteffen.blog.post.{Post, PostRepositoryComponent}
import spray.json.{DefaultJsonProtocol, _}

import scala.concurrent.Future

trait PostsApi extends SprayJsonSupport with DefaultJsonProtocol {
  this: PostRepositoryComponent =>

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val postFormat = jsonFormat5(Post)

  val postRoutes =
    (path("posts") & get) {
      complete(postRepository.findAll.map(_.toJson))
    } ~
    (path("posts" / Segment) & get) { id =>
      val response: Future[ToResponseMarshallable] = postRepository.findById(id).map {
        case Some(post) => post.toJson
        case None => HttpResponse(StatusCodes.NotFound)
      }
      onSuccess(response) { res => complete(res) }
    }
}