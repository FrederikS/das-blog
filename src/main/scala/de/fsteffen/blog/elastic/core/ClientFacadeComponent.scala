package de.fsteffen.blog.elastic.core

import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import de.fsteffen.blog.post.Post
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.search.SearchResponse

import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success, Try}

trait ClientFacadeComponent { this: ClientComponent =>

  val clientFacade:ClientFacade = new ClientFacade

  class ClientFacade {
    import scala.concurrent.ExecutionContext.Implicits.global

    private val _objectMapper: ObjectMapper = new ObjectMapper() with ScalaObjectMapper registerModule DefaultScalaModule

    client.admin().indices().prepareCreate(ClientFacade.Index)
      .addMapping("posts", Source.fromInputStream(getClass.getResourceAsStream("/de/fsteffen/blog/elastic/posts-mapping.json")).mkString)
      .execute().get()

    def saveDocument[T <: Entity](doc: T): Future[Try[String]] = {
      Future {
        Try {
          getTypeForClass(doc.getClass) match {
            case Success(typeForClass) => client.prepareIndex(ClientFacade.Index, typeForClass, doc.getId)
              .setSource(toJson(doc))
              .get
              .getId
            case Failure(ex) => throw ex
          }
        }
      }
    }

    private def getTypeForClass[T <: Entity](clazz: Class[T]): Try[String] = {
      Try {
        ClientFacade.ClassToType.get(clazz) match {
          case Some(typeForClass) => typeForClass
          case None => throw new TypeNotFoundException(s"No elasticsearch type found for class $clazz")
        }
      }
    }

    def getDocument[T <: Entity](id: String, clazz: Class[T]): Future[Try[Option[T]]] = {
      Future {
        Try {
          getTypeForClass(clazz) match {
            case Success(typeForClass) =>
              val response: GetResponse = client.prepareGet(ClientFacade.Index, typeForClass, id).get()
              getDocFromResponse(response, clazz)
            case Failure(ex) => throw ex
          }
        }
      }
    }

    private def getDocFromResponse[T <: Entity](getResponse: GetResponse, clazz: Class[T]): Option[T] = {
      getResponse match {
        case response if response.isSourceEmpty => Option.empty[T]
        case response => Option(fromJson(new SourceAndIdProvider {
          override def getSourceAsMap: util.Map[String, AnyRef] = response.getSourceAsMap
          override def getId: String = response.getId
        }, clazz))
      }
    }

    def getDocuments[T <: Entity](clazz: Class[T]): Future[Try[Seq[T]]] = {
      Future {
        Try {
          getTypeForClass(clazz) match {
            case Success(typeForClass) =>
              val response: SearchResponse = client.prepareSearch(ClientFacade.Index).setTypes(typeForClass).get()
              response.getHits.getHits.map(hit => fromJson(new SourceAndIdProvider {
                override def getSourceAsMap: util.Map[String, AnyRef] = hit.getSource
                override def getId: String = hit.getId
              }, clazz))
                .toSeq
            case Failure(ex) => throw ex
          }
        }
      }
    }

    def deleteDocument[T <: Entity](id: String, clazz: Class[T]): Future[Try[String]] = {
      Future {
        Try{
          getTypeForClass(clazz) match {
            case Success(typeForClass) => client.prepareDelete(ClientFacade.Index, typeForClass, id)
              .get()
              .getId
            case Failure(ex) => throw ex
          }
        }
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

  object ClientFacade {
    val Index = "blog"
    val ClassToType: Map[Class[_ <: Entity], String] = Map(
      classOf[Post] -> "posts"
    )
  }

  class TypeNotFoundException(message: String) extends Throwable

}