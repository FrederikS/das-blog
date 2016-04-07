package de.fsteffen.blog.text

import java.io.File

import com.google.common.io.Files
import de.fsteffen.blog.elastic.core.ClientFacade
import de.fsteffen.blog.elastic.text.ElasticBasedBlogTextRepository
import org.apache.commons.io.FileUtils
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.node.{Node, NodeBuilder}
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

class ElasticBasedBlogTextRepositorySpec extends FunSuite with BeforeAndAfterAll with Matchers {

  var _blogTextRepo:ElasticBasedBlogTextRepository = _
  var _elasticNode: Node = _
  var _tmpDir:File = _

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

  test("testSave") {
    val savedBlogId = _blogTextRepo.save(BlogText(
      content = "content",
      title = "title",
      authorId = 1,
      timestamp = System.currentTimeMillis()
    ))

    savedBlogId should not be empty
  }


  test("testFindByid") {

    val savedBlogText: BlogText = BlogText(
      content = "content",
      title = "title",
      authorId = 1,
      timestamp = System.currentTimeMillis()
    )

    val returnedBlogId = _blogTextRepo.save(savedBlogText)

    val blogForId: BlogText = _blogTextRepo.findById(returnedBlogId)

    blogForId.id should equal(returnedBlogId)
    blogForId.content should equal(savedBlogText.content)
    blogForId.title should equal(savedBlogText.title)
    blogForId.authorId should equal(savedBlogText.authorId)
    blogForId.timestamp should equal(savedBlogText.timestamp)
  }

  test("testUpdateBlog") {
    val returnedBlogId = _blogTextRepo.save(BlogText(
      content = "content",
      title = "title",
      authorId = 1,
      timestamp = System.currentTimeMillis()
    ))

    val updatedBlogText: BlogText = _blogTextRepo.findById(returnedBlogId)
    updatedBlogText.content = "new content"
    _blogTextRepo.save(updatedBlogText)

    val updatedBlogTextFromDb: BlogText = _blogTextRepo.findById(returnedBlogId)
    updatedBlogTextFromDb.id should equal(updatedBlogText.id)
    updatedBlogTextFromDb.content should equal("new content")
  }

}