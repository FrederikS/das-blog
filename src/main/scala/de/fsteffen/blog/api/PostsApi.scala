package de.fsteffen.blog.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import de.fsteffen.blog.elastic.core.Sort
import de.fsteffen.blog.post.{Post, PostQuery, PostRepositoryComponent}
import spray.json.{DefaultJsonProtocol, _}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait PostsApi extends SprayJsonSupport with DefaultJsonProtocol {
  this: PostRepositoryComponent =>

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val postFormat = jsonFormat4(Post)

  val postRoutes =
    path("posts") {
      get {
        parameters("sort".?) { sort =>
          val repoCall = getPostQueryFor(sort) match {
            case Some(query) => postRepository.findBy(query)
            case None => postRepository.findAll()
          }
          val response = answerWithErrorOnExOr(repoCall)(_.toJson)
          onSuccess(response) { res => complete(res) }
        }
      } ~
        post { entity(as[JsObject]) { postData =>
            val response = answerWithErrorOnExOr(postRepository.save(Post(
              content = postData.fields("content").convertTo[String],
              title = postData.fields("title").convertTo[String],
              timestamp = System.currentTimeMillis()
            )))(savedId => savedId)
            onSuccess(response) { res => complete(res) }
          }
        }
    } ~
    path("posts" / Segment) { id =>
      get {
        val response = answerWithErrorOnExOr(postRepository.findById(id)) {
          case Some(post) => post.toJson
          case None => HttpResponse(StatusCodes.NotFound)
        }
        onSuccess(response) { res => complete(res) }
      } ~
      put {
        entity(as[JsObject]) { postData =>
          val response = answerWithErrorOnExOr(postRepository.findById(id)) {
            case Some(postToUpdate) => postRepository.save(Post(
              id = postToUpdate.id,
              content = postData.fields("content").convertTo[String],
              title = postData.fields("title").convertTo[String],
              timestamp = System.currentTimeMillis()
            ))
            case None => HttpResponse(StatusCodes.NotFound)
          }
          onSuccess(response) { res => complete(res) }
        }
      } ~
      delete {
        val response = answerWithErrorOnExOr(postRepository.findById(id)) {
          case Some(postToDelete) => postRepository.delete(postToDelete.id)
          case None => HttpResponse(StatusCodes.NotFound)
        }
        onSuccess(response) { res => complete(res) }
      }
    }

  private def answerWithErrorOnExOr[T](futureOperation: Future[Try[T]])(mapper: T => ToResponseMarshallable): Future[ToResponseMarshallable] = {
    futureOperation map {
      case Success(data) => mapper(data)
      case Failure(ex) => HttpResponse(StatusCodes.InternalServerError, entity = HttpEntity(ex.getMessage))
    }
  }

  private def getPostQueryFor(sortParam: Option[String]): Option[PostQuery] = {
    sortParam match {
      case Some(sort) => sort match {
        case sortString if sortString.startsWith("-") => Option(PostQuery(Sort(sortString.substring(1), ascending = false)))
        case _ => Option(PostQuery(Sort(sort, ascending = true)))
      }
      case None => None
    }
  }

}