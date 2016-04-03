package de.fsteffen.blog.text

import com.fasterxml.jackson.annotation.JsonProperty
import de.fsteffen.blog.elastic.core.Entity

case class BlogText(@JsonProperty("id") id: String = "",
                    @JsonProperty("content") content: String,
                    @JsonProperty("title") title: String,
                    @JsonProperty("authorId") authorId: Int,
                    @JsonProperty("timestamp") timestamp: Long) extends Entity {
  override def getId: String = id
}
