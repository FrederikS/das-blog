package de.fsteffen.blog.elastic.text

import de.fsteffen.blog.elastic.core.ClientFacade
import de.fsteffen.blog.text.{BlogText, BlogTextRepository}

import scala.concurrent.Future

class ElasticBasedBlogTextRepository(clientFacade: ClientFacade) extends BlogTextRepository {

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
