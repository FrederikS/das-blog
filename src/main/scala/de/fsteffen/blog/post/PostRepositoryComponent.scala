package de.fsteffen.blog.post

import scala.concurrent.Future
import scala.util.Try

trait PostRepositoryComponent {
  val postRepository:PostRepository

  trait PostRepository {
    def save(post: Post):Future[Try[String]]
    def findById(id: String):Future[Try[Option[Post]]]
    def findAll():Future[Try[Seq[Post]]]
    def findBy(query: PostQuery):Future[Try[Seq[Post]]]
    def delete(postId: String):Future[Try[String]]
  }
}
