package de.fsteffen.blog.api

import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.fsteffen.blog.post
import de.fsteffen.blog.post.{Post, PostRepositoryComponent}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import spray.json.{JsArray, JsString, _}

import scala.concurrent.Future

class PostsApiSpec extends WordSpec with Matchers with ScalatestRouteTest with PostsApi with PostRepositoryComponent with MockFactory {

  override val postRepository = mock[PostRepository]

  "PostsApi" should {

    "retrieve all posts with GET /posts" in {
      val allPosts = Seq(
        post.Post("1", "content", "title", "1", System.currentTimeMillis()),
        post.Post("2", "content 2", "title 2", "1", System.currentTimeMillis())
      )
      (postRepository.findAll _).expects().returns(Future(allPosts))

      Get("/posts") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
        responseAs[JsArray] should be(allPosts.toJson)
      }
    }

    "retrieve post by id with GET /posts/{id}" in {
      val postForId = post.Post("1", "content", "title", "1", System.currentTimeMillis())
      (postRepository.findById _).expects("1").returns(Future(Option(postForId)))

      Get("/posts/1") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
        responseAs[JsObject] should be(postForId.toJson)
      }
    }

    "return not found when GET /posts/{id} not exists" in {
      (postRepository.findById _).expects("1").returns(Future(Option.empty))

      Get("/posts/1") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 404
      }
    }

    "create post with POST /posts" in {
      val postEntity = HttpEntity(MediaTypes.`application/json`, JsObject(
        "content" -> JsString("content"),
        "title" -> JsString("title"),
        "authorId" -> JsString("1")
      ).toString)

      (postRepository.save _).expects(where { post: Post =>
        post.content.equals("content") &&
        post.title.equals("title") &&
        post.authorId.equals("1") &&
        post.timestamp > 0
      }).returning(Future("1"))

      Post("/posts", postEntity) ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
      }
    }

    "update post with PUT /posts/{id}" in {
      val putEntity = HttpEntity(MediaTypes.`application/json`, JsObject(
        "content" -> JsString("newContent"),
        "title" -> JsString("newTitle"),
        "authorId" -> JsString("1")
      ).toString)

      val postToUpdate: Post = post.Post("1", "content", "title", "1", System.currentTimeMillis())
      (postRepository.findById _).expects("1").returns(Future(Option(postToUpdate)))

      (postRepository.save _).expects(where { updatedPost: Post =>
        updatedPost.id.equals("1") &&
        updatedPost.content.equals("newContent") &&
        updatedPost.title.equals("newTitle") &&
        updatedPost.authorId.equals("1") &&
        updatedPost.timestamp > postToUpdate.timestamp
      }).returning(Future("1"))

      Put("/posts/1", putEntity) ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
      }
    }

    "update post with id not exists should return 404" in {
      val putEntity = HttpEntity(MediaTypes.`application/json`, JsObject(
        "content" -> JsString("content"),
        "title" -> JsString("title"),
        "authorId" -> JsString("1")
      ).toString)

      (postRepository.findById _).expects("1").returns(Future(Option.empty))

      Put("/posts/1", putEntity) ~> postRoutes ~> check {
        response.status.intValue shouldEqual 404
      }
    }

  }

}
