package com.theguardian.content.rules

import cats.data.EitherT
import cats.implicits.*
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.{ItemQuery, SearchQuery}
import com.madgag.scala.collection.decorators.*
import com.theguardian.aws.apigateway.{ApiGatewayRequest, ApiGatewayResponse}
import com.theguardian.capi.CapiId.*
import com.theguardian.capi.{CapiContentCache, CapiId}
import com.theguardian.content.rules.Credentials.fetchKeyFromParameterStore
import com.theguardian.content.rules.logging.Logging
import com.theguardian.content.rules.model.*
import upickle.default.{ReadWriter as RW, *}

import java.io.ByteArrayInputStream
import java.net.URI
import java.net.http.HttpClient.Redirect
import java.net.http.HttpClient.Version.HTTP_2
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.{HttpClient, HttpRequest}
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Clock.systemUTC
import java.time.Duration.ofSeconds
import java.util.Collections
import scala.beans.BeanProperty
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters.*
import scala.jdk.FutureConverters.*
import scala.util.{Failure, Success}
import model.*


object Lambda extends Logging {
  val contentClient: GuardianContentClient = {
    val capiKey = fetchKeyFromParameterStore("capi-key")
    new GuardianContentClient(capiKey)
  }

  private val capiContentCache = new CapiContentCache(contentClient, _.showTags("all"))

  /*
   * Logic handler
   */
  def go(capiId: CapiId, spreadsheetId: String): String = {
    val contentF = capiContentCache.cache.get(capiId)
    val spreadsheetRuleEngineOptF = new SpreadsheetRuleEngineService().ruleEngineFor(spreadsheetId)

    val eventual = for {
      contentOpt <- contentF
      spreadsheetRuleEngineOpt <- spreadsheetRuleEngineOptF
    } yield write(RecommendationResponse(
      contentOpt.map(ContentSummary.summaryFor),
      spreadsheetRuleEngineOpt.map(_.spreadsheetSummary),
      for {
        content <- contentOpt
        spreadsheetRuleEngine <- spreadsheetRuleEngineOpt
      } yield spreadsheetRuleEngine.ruleEngine.findRecommendations(model.Context(content), 5)
    ))

    Await.result(eventual, 40.seconds)
  }

  /*
   * Lambda's entry point
   */
  def handler(request: ApiGatewayRequest): ApiGatewayResponse = ApiGatewayResponse(200, Map(
    "Content-Type" -> "application/json",
    "Cache-Control" -> "max-age=20, stale-while-revalidate=1, stale-if-error=864000",
    "X-Git-Commit-Id" -> BuildInfo.gitCommitId
  ), go(
    CapiId(request.queryStringParamMap("capi-id")),
    request.queryStringParamMap("spreadsheet-id"))
  )
}
