package de.fsteffen.blog.text

import scala.concurrent.Future

trait BlogTextRepositoryComponent {
  val blogTextRepository:BlogTextRepository

  trait BlogTextRepository {
    def save(blogText: BlogText):Future[String]
    def findById(id:String):Future[BlogText]
    def findAll:Future[Seq[BlogText]]
  }
}
