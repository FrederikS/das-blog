package de.fsteffen.blog.post

import de.fsteffen.blog.elastic.core.ClientFacadeComponent
import de.fsteffen.blog.elastic.post.ElasticBasedPostRepositoryComponent

trait ElasticPostRepositoryEnvironment extends ElasticBasedPostRepositoryComponent with ClientFacadeComponent with ElasticClientEnvironment {
}
