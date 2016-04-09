package de.fsteffen.blog.text

import de.fsteffen.blog.elastic.core.ClientFacadeComponent
import de.fsteffen.blog.elastic.text.ElasticBasedBlogTextRepositoryComponent

trait ElasticBlogTextRepositoryEnvironment extends ElasticBasedBlogTextRepositoryComponent with ClientFacadeComponent with ElasticClientEnvironment {
}
