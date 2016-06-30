package de.fsteffen.blog.api

import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.fsteffen.blog.post
import de.fsteffen.blog.post.{Post, PostQuery, PostRepositoryComponent}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import spray.json.{JsArray, JsString, _}
import com.github.nscala_time.time.Imports._
import de.fsteffen.blog.elastic.core.Sort

import scala.concurrent.Future
import scala.util.Try

class PostsApiSpec extends WordSpec with Matchers with ScalatestRouteTest with PostsApi with PostRepositoryComponent with MockFactory {

  override val postRepository = mock[PostRepository]

  "PostsApi" should {

    "retrieve all posts with GET /posts" in {
      val allPosts = Seq(
        post.Post("1", "content", "title", System.currentTimeMillis()),
        post.Post("2", "content 2", "title 2", System.currentTimeMillis())
      )
      (postRepository.findAll _).expects().returns(Future(Try(allPosts)))

      Get("/posts") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
        responseAs[JsArray] should be(allPosts.toJson)
      }
    }

    "retrieve post by id with GET /posts/{id}" in {
      val postForId = post.Post("1", "content", "title", System.currentTimeMillis())
      (postRepository.findById _).expects("1").returns(Future(Try(Option(postForId))))

      Get("/posts/1") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
        responseAs[JsObject] should be(postForId.toJson)
      }
    }

    "return not found when GET /posts/{id} not exists" in {
      (postRepository.findById _).expects("1").returns(Future(Try(Option.empty)))

      Get("/posts/1") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 404
      }
    }

    "create post with POST /posts" in {
      val postEntity = HttpEntity(MediaTypes.`application/json`, JsObject(
        "content" -> JsString("content"),
        "title" -> JsString("title")
      ).toString)

      (postRepository.save _).expects(where { post: Post =>
        post.content.equals("content") &&
        post.title.equals("title") &&
        post.timestamp > 0
      }).returning(Future(Try("1")))

      Post("/posts", postEntity) ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
      }
    }

    "update post with PUT /posts/{id}" in {
      val putEntity = HttpEntity(MediaTypes.`application/json`, JsObject(
        "content" -> JsString("newContent"),
        "title" -> JsString("newTitle")
      ).toString)

      val postToUpdate: Post = post.Post("1", "content", "title", System.currentTimeMillis())
      (postRepository.findById _).expects("1").returns(Future(Try(Option(postToUpdate))))

      (postRepository.save _).expects(where { updatedPost: Post =>
        updatedPost.id.equals("1") &&
        updatedPost.content.equals("newContent") &&
        updatedPost.title.equals("newTitle") &&
        updatedPost.timestamp > postToUpdate.timestamp
      }).returning(Future(Try("1")))

      Put("/posts/1", putEntity) ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
      }
    }

    "update post with id not exists should return 404" in {
      val putEntity = HttpEntity(MediaTypes.`application/json`, JsObject(
        "content" -> JsString("content"),
        "title" -> JsString("title")
      ).toString)

      (postRepository.findById _).expects("1").returns(Future(Try(Option.empty)))

      Put("/posts/1", putEntity) ~> postRoutes ~> check {
        response.status.intValue shouldEqual 404
      }
    }

    "delete postId with DELETE /posts/{id}" in {
      (postRepository.findById _).expects("1").returns(Future(Try(Option(
        post.Post("1", "content", "title", System.currentTimeMillis())
      ))))
      (postRepository.delete _).expects("1").returns(Future(Try("1")))

      Delete("/posts/1") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
      }
    }

    "delete postId with id not exists should return 404" in {
      (postRepository.findById _).expects("1").returns(Future(Try(Option.empty)))

      Delete("/posts/1") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 404
      }
    }

    "retrieve all posts sorted by date ascending" in {
      val post1 = post.Post("1", "content", "title", DateTime.now.getMillis)
      val post2 = post.Post("2", "content 2", "title 2", DateTime.now.plusDays(1).getMillis)

      (postRepository.findBy _).expects(PostQuery(Sort("date", ascending = true))).returns(Future(Try(Seq())))

      Get("/posts?sort=date") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
      }
    }

    "retrieve all posts sorted by date descending" in {
      val post1 = post.Post("1", "content", "title", DateTime.now.getMillis)
      val post2 = post.Post("2", "content 2", "title 2", DateTime.now.minusDays(1).getMillis)

      (postRepository.findBy _).expects(PostQuery(Sort("date", ascending = false))).returns(Future(Try(Seq())))

      Get("/posts?sort=-date") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
      }
    }

  }

}
