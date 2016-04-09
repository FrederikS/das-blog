package de.fsteffen.blog.text

import org.apache.commons.io.FileUtils
import org.scalatest.concurrent.Futures
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class ElasticBasedBlogTextRepositorySpec extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with Futures with ElasticClientEnvironment with ElasticBlogTextRepositoryEnvironment {

  import scala.concurrent.ExecutionContext.Implicits.global

  override protected def afterAll(): Unit = {
    elasticNode.close()
    FileUtils.deleteDirectory(tmpDir)
  }

  override protected def afterEach(): Unit = {
    elasticNode.client().admin().indices().prepareDelete("test").execute().actionGet()
  }

  test("testSave") {
    val savedBlogId: String = Await.result(blogTextRepository.save(ElasticBasedBlogTextRepositorySpec.anyBlogText), 1.second)
    savedBlogId should not be empty
  }

  test("testFindByid") {
    val savedBlogText: BlogText = BlogText(
      content = "content",
      title = "title",
      authorId = 1,
      timestamp = System.currentTimeMillis()
    )

    Await.result(for {
      returnedBlogId <- blogTextRepository.save(savedBlogText)
      blogForId <- blogTextRepository.findById(returnedBlogId)
    } yield {
      blogForId.id should equal(returnedBlogId)
      blogForId.content should equal(savedBlogText.content)
      blogForId.title should equal(savedBlogText.title)
      blogForId.authorId should equal(savedBlogText.authorId)
      blogForId.timestamp should equal(savedBlogText.timestamp)
    }, 1.second)

  }

  test("testUpdateBlog") {
    Await.result(for {
      returnedBlogId <- blogTextRepository.save(ElasticBasedBlogTextRepositorySpec.anyBlogText)
      blogText <- blogTextRepository.findById(returnedBlogId)
    } yield {
      blogText.content = "new content"
      Await.result(blogTextRepository.save(blogText), 1.second)
      val updatedBlogTextFromDb: BlogText = Await.result(blogTextRepository.findById(returnedBlogId), 1.second)
      updatedBlogTextFromDb.id should equal(blogText.id)
      updatedBlogTextFromDb.content should equal("new content")
    }, 1.second)
  }

  test("testFindAll") {
    Await.result(for {
      id1 <- blogTextRepository.save(ElasticBasedBlogTextRepositorySpec.anyBlogText)
      id2 <- blogTextRepository.save(ElasticBasedBlogTextRepositorySpec.anyBlogText)
    } yield {
      elasticNode.client().admin().indices().prepareRefresh().execute().actionGet()
      val allBlogTexts: Seq[BlogText] = Await.result(blogTextRepository.findAll, 1.second)
      allBlogTexts should have size 2
    }, 1.second)
  }

  private object ElasticBasedBlogTextRepositorySpec {
    def anyBlogText: BlogText = {
      BlogText(
        content = "content",
        title = "title",
        authorId = 1,
        timestamp = System.currentTimeMillis()
      )
    }
  }

}
