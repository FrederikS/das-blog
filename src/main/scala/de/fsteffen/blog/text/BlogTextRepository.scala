package de.fsteffen.blog.text

trait BlogTextRepository {

  def findById(id:String):BlogText
  def findAll:Seq[BlogText]

}
