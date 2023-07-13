package com.theguardian.content.rules

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.gu.contentapi.client.ContentApiClient
import com.gu.contentapi.client.model.v1.Content
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import com.theguardian.Caching
import com.theguardian.Caching.cachingMissingResults
import com.theguardian.content.rules.SpreadsheetService.rowDataFrom

import scala.concurrent.duration.{FiniteDuration, *}
import scala.concurrent.{ExecutionContext, Future}
import model.*

import scala.util.Failure

case class SpreadsheetRuleEngine(
  spreadsheetSummary: SpreadsheetSummary,
  ruleEngine: RuleEngine
)

class SpreadsheetRuleEngineService(using ExecutionContext) {

  val cache: AsyncLoadingCache[String, Option[SpreadsheetRuleEngine]] = cachingMissingResults(30.seconds, 2.hours)
    .refreshAfterWrite(30.seconds) // asynchronously refresh when the key is requested and is over this age
    .maximumSize(1000) // we probably don't expect more than 1000 different spreadsheets...!
    .buildAsyncFuture(singleLoad)

  private def singleLoad(spreadsheetId: String): Future[Option[SpreadsheetRuleEngine]] = (for {
    spreadsheet <- SpreadsheetService.getSpreadsheet(spreadsheetId)
  } yield {
    val ruleEngine = RuleEngine.fromGrid(rowDataFrom(spreadsheet))
    Some(SpreadsheetRuleEngine(SpreadsheetSummary.summaryFor(spreadsheet), ruleEngine))
  }).recover {
    case e: GoogleJsonResponseException =>
      println(s"e.getDetails.getMessage=${e.getDetails.getMessage}")
      None
  }

  def ruleEngineFor(spreadsheetId: String): Future[Option[SpreadsheetRuleEngine]] = cache.get(spreadsheetId)
}
