package de.fsteffen.blog.text

import scala.concurrent.Future

trait BlogTextRepository {

  def findById(id:String):Future[BlogText]
  def findAll:Future[Seq[BlogText]]

}
