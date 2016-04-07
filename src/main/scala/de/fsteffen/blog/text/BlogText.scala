package de.fsteffen.blog.text

import com.fasterxml.jackson.annotation.JsonProperty
import de.fsteffen.blog.elastic.core.Entity

case class BlogText(@JsonProperty("id") id: String = null,
                    @JsonProperty("content") var content: String,
                    @JsonProperty("title") var title: String,
                    @JsonProperty("authorId") authorId: Int,
                    @JsonProperty("timestamp") timestamp: Long) extends Entity {
  override def getId: String = id
}
