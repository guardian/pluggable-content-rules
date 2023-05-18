package com.theguardian.content.rules

import Credentials.fetchKeyFromParameterStore
import cats.data.EitherT
import cats.implicits.*
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.ItemQuery
import com.madgag.scala.collection.decorators.*
import com.theguardian.aws.apigateway.ApiGatewayRequest
import com.theguardian.aws.apigateway.ApiGatewayResponse
import com.theguardian.content.rules.logging.Logging
import upickle.default._
import upickle.default.{ReadWriter => RW, macroRW}

import java.io.ByteArrayInputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect
import java.net.http.HttpClient.Version.HTTP_2
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Clock.systemUTC
import java.time.Duration.ofSeconds
import java.util.Collections
import scala.beans.BeanProperty
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*
import scala.jdk.FutureConverters.*
import scala.util.Failure
import scala.util.Success

case class RecomendationResponse(capiId:String, webTitle:String, recommendations: Seq[String])
object RecomendationResponse{
  implicit val rw: RW[RecomendationResponse] = macroRW
}  
    
object Lambda extends Logging {
//
//  val googleSearchService: GoogleSearchService = {
//    val apiKey = fetchKeyFromParameterStore("Google/CustomSearch/ApiKey")
//    new GoogleSearchService(apiKey)
//  }

  val contentClient: GuardianContentClient = {
    val capiKey = fetchKeyFromParameterStore("capi-key")
    new GuardianContentClient(capiKey)
  }

  /*
   * Logic handler
   */
  def go(capiId:String, spreadsheetId: String): String = {

    val ruleData = SpreadsheetService.getData(spreadsheetId)
    //ruleData.foreach(rule => rule.foreach(println(_)))
    println(ruleData.mkString("\n"))
    val ruleEngine = RuleEngine.fromGrid(ruleData)

    val eventual = for {
      response <- contentClient.getResponse(ItemQuery(capiId).showTags("all"))
    } yield {
      val content = response.content.get
      write(RecomendationResponse(capiId, content.webTitle, ruleEngine.findRecommendations(com.theguardian.content.rules.model.Context(content), 5)))
    }

    Await.result(eventual, 40.seconds)
  }

  /*
   * Lambda's entry point
   */
  def handler(request: ApiGatewayRequest): ApiGatewayResponse = {
    ApiGatewayResponse(200, Map("Content-Type"->"application/json", "Cache-Control"->"max-age=20, stale-while-revalidate=6, stale-if-error=864000"
    ) , go(request.queryStringParamMap("capi-id"), request.queryStringParamMap("spreadsheet-id")))
  }

}
