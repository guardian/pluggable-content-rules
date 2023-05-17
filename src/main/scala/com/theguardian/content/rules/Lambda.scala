package com.theguardian.content.rules

import cats.data.EitherT
import cats.implicits.*
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.madgag.scala.collection.decorators.*
import com.theguardian.content.rules.logging.Logging
import Credentials.fetchKeyFromParameterStore
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
  def go(capiId:String): String = {


    val eventual = for {

      response <- contentClient.getResponse(ItemQuery(capiId))
    } yield response.content.get.webTitle

    Await.result(eventual, 40.seconds)
  }

  /*
   * Lambda's entry point
   */
  def handler(request: ApiGatewayRequest): ApiGatewayResponse = {
    ApiGatewayResponse(200, Map.empty, go(request.queryStringParamMap("capi-id")))
  }

}
