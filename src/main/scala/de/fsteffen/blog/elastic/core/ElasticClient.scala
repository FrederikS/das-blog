package de.fsteffen.blog.elastic.core
import java.net.InetAddress

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress

trait ElasticClient extends ClientComponent {
  override val client: TransportClient = TransportClient.builder().build()
    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300))
}
