package de.fsteffen.blog.api

import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.fsteffen.blog.post
import de.fsteffen.blog.post.PostRepositoryComponent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import spray.json.{JsArray, _}

import scala.concurrent.Future

class PostsApiSpec extends WordSpec with Matchers with ScalatestRouteTest with PostsApi with PostRepositoryComponent with MockFactory {

  override val postRepository = stub[PostRepository]

  "UsersApi" should {

    "retrieve all posts with GET /posts" in {
      val allPosts = Seq(
        post.Post("1", "content", "title", 1, System.currentTimeMillis()),
        post.Post("2", "content 2", "title 2", 1, System.currentTimeMillis())
      )

      (postRepository.findAll _).when().returns(Future(allPosts))
      Get("/posts") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
        responseAs[JsArray] should be(allPosts.toJson)
      }
    }

    "retrieve post by id with GET /posts/{id}" in {
      val postForId = post.Post("1", "content", "title", 1, System.currentTimeMillis())

      (postRepository.findById _).when("1").returns(Future(Option(postForId)))
      (postRepository.findById _).when(*).returns(Future(Option.empty))

      Get("/posts/1") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 200
        responseAs[JsObject] should be(postForId.toJson)
      }

      Get("/posts/2") ~> postRoutes ~> check {
        response.status.intValue shouldEqual 404
      }
    }

    "create post with POST /posts" in {

    }

  }

}
