package de.fsteffen.blog.elastic.core

import org.elasticsearch.client.Client

trait ClientComponent {
  val client:Client
}
