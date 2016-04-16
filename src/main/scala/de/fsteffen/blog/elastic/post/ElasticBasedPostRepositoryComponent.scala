package de.fsteffen.blog.elastic.post

import de.fsteffen.blog.elastic.core.ClientFacadeComponent
import de.fsteffen.blog.post.{Post, PostRepositoryComponent}

import scala.concurrent.Future

trait ElasticBasedPostRepositoryComponent extends PostRepositoryComponent { this:ClientFacadeComponent =>

  override val postRepository = new ElasticBasedPostRepository

  class ElasticBasedPostRepository extends PostRepository {
    def save(post: Post):Future[String] = {
      clientFacade.saveDocument("test", "posts", post)
    }

    override def findById(id: String): Future[Option[Post]] = {
      clientFacade.getDocument("test", "posts", id, classOf[Post])
    }

    override def findAll: Future[Seq[Post]] = {
      clientFacade.getDocuments("test", "posts", classOf[Post])
    }

    override def delete(postId: String): Future[String] = {
      clientFacade.deleteDocument("test", "posts", postId)
    }
  }

}
