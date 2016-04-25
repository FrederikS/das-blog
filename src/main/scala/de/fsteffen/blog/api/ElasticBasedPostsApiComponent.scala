package de.fsteffen.blog.api

import java.net.InetAddress

import de.fsteffen.blog.elastic.core.{ClientComponent, ClientFacadeComponent}
import de.fsteffen.blog.elastic.post.ElasticBasedPostRepositoryComponent
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

trait ElasticBasedPostsApiComponent extends PostsApi with ElasticBasedPostRepositoryComponent with ClientFacadeComponent with ClientComponent {
  override val client: TransportClient = TransportClient.builder().build()
    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))
}