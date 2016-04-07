package de.fsteffen.blog.elastic.core

import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.client.Client

import scala.io.Source

class ClientFacade(client: Client) {

  private val _objectMapper:ObjectMapper = new ObjectMapper() with ScalaObjectMapper registerModule DefaultScalaModule

  client.admin().indices().prepareCreate("test")
    .addMapping("blog", Source.fromInputStream(getClass.getResourceAsStream("/de/fsteffen/blog/elastic/blogtext-mapping.json")).mkString)
    .execute().get()

  def saveDocument[T <: Entity](index: String, typee: String, doc: T):String = {
    client.prepareIndex(index, typee, doc.getId)
      .setSource(toJson(doc))
      .get
      .getId
  }

  def getDocument[T <: Entity](index: String, typee: String, id: String, clazz: Class[T]): T = {
    fromJson(client.prepareGet(index, typee, id)
      .get(), clazz)
  }

  private def toJson(doc: Any): Array[Byte] = {
    _objectMapper.writeValueAsBytes(doc)
  }

  private def fromJson[T <: Entity](response: GetResponse, clazz: Class[T]): T = {
    val values: util.Map[String, AnyRef] = response.getSourceAsMap
    values.put("id", response.getId)
    _objectMapper.convertValue(values, clazz)
  }

}
