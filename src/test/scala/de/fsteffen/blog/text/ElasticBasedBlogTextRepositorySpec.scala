package de.fsteffen.blog.text

import java.io.File

import com.google.common.io.Files
import de.fsteffen.blog.elastic.core.ClientFacade
import de.fsteffen.blog.elastic.text.ElasticBasedBlogTextRepository
import org.apache.commons.io.FileUtils
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.node.{Node, NodeBuilder}
import org.scalatest.concurrent.Futures
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class ElasticBasedBlogTextRepositorySpec extends FunSuite with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with Futures {

  import scala.concurrent.ExecutionContext.Implicits.global

  var _blogTextRepo: ElasticBasedBlogTextRepository = _
  var _elasticNode: Node = _
  var _tmpDir: File = _

  override protected def beforeAll(): Unit = {
    _tmpDir = Files.createTempDir()
    _elasticNode = NodeBuilder.nodeBuilder()
      .local(true)
      .settings(Settings.builder()
        .put("http.enabled", "false")
        .put("path.data", _tmpDir.toString + "/data")
        .put("path.home", _tmpDir.toString)
        .put("cluster.name", "elasticsearch")
        .build()
      ).build()
    _elasticNode.start()
    _blogTextRepo = new ElasticBasedBlogTextRepository(new ClientFacade(_elasticNode.client))
  }

  override protected def afterAll(): Unit = {
    _elasticNode.close()
    FileUtils.deleteDirectory(_tmpDir)
  }

  override protected def afterEach(): Unit = {
    _elasticNode.client().admin().indices().prepareDelete("test").execute().actionGet()
  }

  test("testSave") {
    val savedBlogId: String = Await.result(_blogTextRepo.save(ElasticBasedBlogTextRepositorySpec.anyBlogText), 1.second)
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
      returnedBlogId <- _blogTextRepo.save(savedBlogText)
      blogForId <- _blogTextRepo.findById(returnedBlogId)
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
      returnedBlogId <- _blogTextRepo.save(ElasticBasedBlogTextRepositorySpec.anyBlogText)
      blogText <- _blogTextRepo.findById(returnedBlogId)
    } yield {
      blogText.content = "new content"
      Await.result(_blogTextRepo.save(blogText), 1.second)
      val updatedBlogTextFromDb: BlogText = Await.result(_blogTextRepo.findById(returnedBlogId), 1.second)
      updatedBlogTextFromDb.id should equal(blogText.id)
      updatedBlogTextFromDb.content should equal("new content")
    }, 1.second)
  }

  test("testFindAll") {
    Await.result(for {
      id1 <- _blogTextRepo.save(ElasticBasedBlogTextRepositorySpec.anyBlogText)
      id2 <- _blogTextRepo.save(ElasticBasedBlogTextRepositorySpec.anyBlogText)
    } yield {
      _elasticNode.client().admin().indices().prepareRefresh().execute().actionGet()
      val allBlogTexts: Seq[BlogText] = Await.result(_blogTextRepo.findAll, 1.second)
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
