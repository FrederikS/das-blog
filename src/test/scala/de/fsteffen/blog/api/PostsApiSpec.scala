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
        responseAs[JsArray] should be(allPosts.toJson)
      }
    }

  }

}
