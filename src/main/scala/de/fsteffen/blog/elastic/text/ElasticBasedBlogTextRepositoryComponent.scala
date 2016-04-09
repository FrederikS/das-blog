package de.fsteffen.blog.elastic.text

import de.fsteffen.blog.elastic.core.ClientFacadeComponent
import de.fsteffen.blog.text.{BlogText, BlogTextRepositoryComponent}

import scala.concurrent.Future

trait ElasticBasedBlogTextRepositoryComponent extends BlogTextRepositoryComponent { this:ClientFacadeComponent =>

  override val blogTextRepository = new ElasticBasedBlogTextRepository

  class ElasticBasedBlogTextRepository extends BlogTextRepository {
    def save(blogText: BlogText):Future[String] = {
      clientFacade.saveDocument("test", "blog", blogText)
    }

    override def findById(id: String): Future[BlogText] = {
      clientFacade.getDocument("test", "blog", id, classOf[BlogText])
    }

    override def findAll: Future[Seq[BlogText]] = {
      clientFacade.getDocuments("test", "blog", classOf[BlogText])
    }
  }

}
