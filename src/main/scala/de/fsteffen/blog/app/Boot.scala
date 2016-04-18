package de.fsteffen.blog.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}

object Boot extends App with Routes {

  private implicit val system = ActorSystem()
  protected implicit val materializer: ActorMaterializer = ActorMaterializer()
  private val config: Config = ConfigFactory.load()
  private val httpConfig: Config = config.getConfig("http")

  Http().bindAndHandle(Route.handlerFlow(routes), httpConfig.getString("host"), httpConfig.getInt("port"))
}
