package de.fsteffen.blog.elastic.post

import de.fsteffen.blog.elastic.core.ClientFacadeComponent
import de.fsteffen.blog.post.{Post, PostRepositoryComponent}

import scala.concurrent.Future

trait ElasticBasedPostRepositoryComponent extends PostRepositoryComponent { this:ClientFacadeComponent =>

  override val postRepository = new ElasticBasedPostRepository

  class ElasticBasedPostRepository extends PostRepository {
    def save(blogText: Post):Future[String] = {
      clientFacade.saveDocument("test", "blog", blogText)
    }

    override def findById(id: String): Future[Option[Post]] = {
      clientFacade.getDocument("test", "blog", id, classOf[Post])
    }

    override def findAll: Future[Seq[Post]] = {
      clientFacade.getDocuments("test", "blog", classOf[Post])
    }
  }

}
