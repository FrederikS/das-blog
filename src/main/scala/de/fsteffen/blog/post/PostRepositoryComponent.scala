package de.fsteffen.blog.post

import scala.concurrent.Future

trait PostRepositoryComponent {
  val postRepository:PostRepository

  trait PostRepository {
    def save(blogText: Post):Future[String]
    def findById(id:String):Future[Post]
    def findAll:Future[Seq[Post]]
  }
}
