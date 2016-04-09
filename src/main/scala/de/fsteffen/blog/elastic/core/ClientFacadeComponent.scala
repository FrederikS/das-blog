package de.fsteffen.blog.elastic.core

import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.SearchResponse

import scala.concurrent.Future
import scala.io.Source

trait ClientFacadeComponent { this: ClientComponent =>

  val clientFacade:ClientFacade = new ClientFacade

  class ClientFacade {
    import scala.concurrent.ExecutionContext.Implicits.global

    private val _objectMapper: ObjectMapper = new ObjectMapper() with ScalaObjectMapper registerModule DefaultScalaModule

    client.admin().indices().prepareCreate("test")
      .addMapping("blog", Source.fromInputStream(getClass.getResourceAsStream("/de/fsteffen/blog/elastic/blogtext-mapping.json")).mkString)
      .execute().get()

    def saveDocument[T <: Entity](index: String, typee: String, doc: T): Future[String] = {
      Future {
        client.prepareIndex(index, typee, doc.getId)
          .setSource(toJson(doc))
          .get
          .getId
      }
    }

    def getDocument[T <: Entity](index: String, typee: String, id: String, clazz: Class[T]): Future[T] = {
      Future {
        val response: GetResponse = client.prepareGet(index, typee, id).get()
        fromJson(new SourceAndIdProvider {
          override def getSourceAsMap: util.Map[String, AnyRef] = response.getSourceAsMap
          override def getId: String = response.getId
        }, clazz)
      }
    }

    def getDocuments[T <: Entity](index: String, typee: String, clazz: Class[T]): Future[Seq[T]] = {
      Future {
        val response: SearchResponse = client.prepareSearch(index).setTypes(typee).get()
        response.getHits.getHits.map(hit => fromJson(new SourceAndIdProvider {
          override def getSourceAsMap: util.Map[String, AnyRef] = hit.getSource
          override def getId: String = hit.getId
        }, clazz))
          .toSeq
      }
    }

    private def toJson(doc: Any): Array[Byte] = {
      _objectMapper.writeValueAsBytes(doc)
    }

    private def fromJson[T <: Entity](data: SourceAndIdProvider, clazz: Class[T]): T = {
      val values: util.Map[String, AnyRef] = data.getSourceAsMap
      values.put("id", data.getId)
      _objectMapper.convertValue(values, clazz)
    }

    private trait SourceAndIdProvider {
      def getId: String
      def getSourceAsMap: util.Map[String, AnyRef]
    }

  }

}