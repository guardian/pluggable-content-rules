package com.theguardian.content.rules

import cats.data.EitherT
import cats.implicits.*
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.madgag.scala.collection.decorators.*
import com.theguardian.content.rules.logging.Logging
import Credentials.fetchKeyFromParameterStore
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import com.theguardian.aws.apigateway.{ApiGatewayRequest, ApiGatewayResponse}
import com.gu.contentapi.client.GuardianContentClient

import java.net.URI
import java.net.http.HttpClient.Redirect
import java.net.http.HttpClient.Version.HTTP_2
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.{HttpClient, HttpRequest}
import java.time.Clock
import java.time.Clock.systemUTC
import java.time.Duration.ofSeconds
import scala.beans.BeanProperty
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}
import scala.jdk.FutureConverters.*
import scala.util.{Failure, Success}
import scala.jdk.CollectionConverters.*
import com.gu.contentapi.client.model.ItemQuery

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.Collections

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

      response <- contentClient.getResponse(ItemQuery(capiId))
    } yield {
      ruleEngine.findRecommendations(com.theguardian.content.rules.model.Context(response.content.get)).mkString(",")
    }

    Await.result(eventual, 40.seconds)
  }

  /*
   * Lambda's entry point
   */
  def handler(request: ApiGatewayRequest): ApiGatewayResponse = {
    ApiGatewayResponse(200, Map.empty, go(request.queryStringParamMap("capi-id"), request.queryStringParamMap("spreadsheet-id")))
  }

}
