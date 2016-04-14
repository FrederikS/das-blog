package de.fsteffen.blog.post

import de.fsteffen.blog.elastic.core.ClientFacadeComponent
import de.fsteffen.blog.elastic.post.ElasticBasedPostRepositoryComponent
import org.apache.commons.io.FileUtils
import org.scalatest.concurrent.Futures
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class ElasticBasedPostRepositorySpec extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with Futures with ElasticClientEnvironment with ElasticBasedPostRepositoryComponent with ClientFacadeComponent {

  import scala.concurrent.ExecutionContext.Implicits.global

  override protected def afterAll(): Unit = {
    elasticNode.close()
    FileUtils.deleteDirectory(tmpDir)
  }

  override protected def afterEach(): Unit = {
    elasticNode.client().admin().indices().prepareDelete("test").execute().actionGet()
  }

  test("testSave") {
    val savedBlogId: String = Await.result(postRepository.save(ElasticBasedBlogTextRepositorySpec.anyBlogText), 1.second)
    savedBlogId should not be empty
  }

  test("testFindByid") {
    val savedBlogText: Post = Post(
      content = "content",
      title = "title",
      authorId = "1",
      timestamp = System.currentTimeMillis()
    )

    Await.result(for {
      returnedBlogId <- postRepository.save(savedBlogText)
      blogForId <- postRepository.findById(returnedBlogId)
    } yield {
      blogForId.get.id should equal(returnedBlogId)
      blogForId.get.content should equal(savedBlogText.content)
      blogForId.get.title should equal(savedBlogText.title)
      blogForId.get.authorId should equal(savedBlogText.authorId)
      blogForId.get.timestamp should equal(savedBlogText.timestamp)
    }, 1.second)

  }

  test("testUpdateBlog") {
    Await.result(for {
      returnedBlogId <- postRepository.save(ElasticBasedBlogTextRepositorySpec.anyBlogText)
      blogText <- postRepository.findById(returnedBlogId)
    } yield {
      blogText.get.content = "new content"
      Await.result(postRepository.save(blogText.get), 1.second)
      val updatedBlogTextFromDb: Post = Await.result(postRepository.findById(returnedBlogId), 1.second).get
      updatedBlogTextFromDb.id should equal(blogText.get.id)
      updatedBlogTextFromDb.content should equal("new content")
    }, 1.second)
  }

  test("testFindAll") {
    Await.result(for {
      id1 <- postRepository.save(ElasticBasedBlogTextRepositorySpec.anyBlogText)
      id2 <- postRepository.save(ElasticBasedBlogTextRepositorySpec.anyBlogText)
    } yield {
      elasticNode.client().admin().indices().prepareRefresh().execute().actionGet()
      val allBlogTexts: Seq[Post] = Await.result(postRepository.findAll, 1.second)
      allBlogTexts should have size 2
    }, 1.second)
  }

  private object ElasticBasedBlogTextRepositorySpec {
    def anyBlogText: Post = {
      Post(
        content = "content",
        title = "title",
        authorId = "1",
        timestamp = System.currentTimeMillis()
      )
    }
  }

}
