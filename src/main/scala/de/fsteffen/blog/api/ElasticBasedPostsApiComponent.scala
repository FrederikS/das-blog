package de.fsteffen.blog.api

import de.fsteffen.blog.elastic.core.{ClientFacadeComponent, ElasticClient}
import de.fsteffen.blog.elastic.post.ElasticBasedPostRepositoryComponent

trait ElasticBasedPostsApiComponent extends PostsApi with ElasticClient with ElasticBasedPostRepositoryComponent with ClientFacadeComponent {
}