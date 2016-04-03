package de.fsteffen.blog.elastic.text

import de.fsteffen.blog.elastic.core.ClientFacade
import de.fsteffen.blog.text.{BlogText, BlogTextRepository}

class ElasticBasedBlogTextRepository(clientFacade: ClientFacade) extends BlogTextRepository {

  def save(blogText: BlogText):String = {
    clientFacade.saveDocument("test", "blog", blogText)
  }

  override def findById(id: String): BlogText = {
    clientFacade.getDocument("test", "blog", id, classOf[BlogText])
  }

}
