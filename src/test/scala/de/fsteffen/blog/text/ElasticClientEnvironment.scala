package de.fsteffen.blog.text

import java.io.File

import com.google.common.io.Files
import de.fsteffen.blog.elastic.core.ClientComponent
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.node.{Node, NodeBuilder}

trait ElasticClientEnvironment extends ClientComponent {

  val tmpDir:File = Files.createTempDir()
  val elasticNode: Node = {
    NodeBuilder.nodeBuilder()
      .local(true)
      .settings(Settings.builder()
        .put("http.enabled", "false")
        .put("path.data", tmpDir.toString + "/data")
        .put("path.home", tmpDir.toString)
        .put("cluster.name", "elasticsearch")
        .build()
      ).build()
  }

  elasticNode.start()

  override val client = {
    elasticNode.client()
  }

}
