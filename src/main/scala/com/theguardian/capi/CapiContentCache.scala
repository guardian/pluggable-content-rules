package com.theguardian.capi

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.gu.contentapi.client.ContentApiClient
import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import com.theguardian.Caching
import com.theguardian.Caching.cachingMissingResults

import scala.concurrent.duration.{FiniteDuration, *}
import scala.concurrent.{ExecutionContext, Future}



class CapiContentCache(
  contentApi: ContentApiClient,
  modifyQuery: ItemQuery => ItemQuery
)(implicit
  ec: ExecutionContext
) {

  private def singleLoad(key: CapiId): Future[Option[Content]] = {
    val itemQuery = modifyQuery(ItemQuery(key.value))
    contentApi.getResponse(itemQuery).map(_.content)
  }

  val cache: AsyncLoadingCache[CapiId, Option[Content]] = cachingMissingResults(30.seconds, 2.hours)
    .refreshAfterWrite(5.minutes) // asynchronously refresh when the key is requested and is over this age
    .maximumSize(100000)
    .buildAsyncFuture(singleLoad)
}
