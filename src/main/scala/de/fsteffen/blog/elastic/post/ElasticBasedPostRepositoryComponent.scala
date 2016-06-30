package de.fsteffen.blog.elastic.post

import de.fsteffen.blog.elastic.core.ClientFacadeComponent
import de.fsteffen.blog.post.{Post, PostQuery, PostRepositoryComponent}

import scala.concurrent.Future
import scala.util.Try

trait ElasticBasedPostRepositoryComponent extends PostRepositoryComponent { this:ClientFacadeComponent =>

  override val postRepository = new ElasticBasedPostRepository

  class ElasticBasedPostRepository extends PostRepository {
    def save(post: Post):Future[Try[String]] = {
      clientFacade.saveDocument(post)
    }

    override def findById(id: String): Future[Try[Option[Post]]] = {
      clientFacade.getDocument(id, classOf[Post])
    }

    override def findAll(): Future[Try[Seq[Post]]] = {
      clientFacade.getDocuments(classOf[Post])
    }

    override def findBy(query: PostQuery): Future[Try[Seq[Post]]] = {
      clientFacade.getDocuments(classOf[Post])
    }

    override def delete(postId: String): Future[Try[String]] = {
      clientFacade.deleteDocument(postId, classOf[Post])
    }
  }

}
