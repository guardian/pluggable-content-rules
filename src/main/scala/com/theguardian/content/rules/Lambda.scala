package com.theguardian.content.rules

import cats.data.EitherT
import cats.implicits.*
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.madgag.scala.collection.decorators.*
import com.theguardian.content.rules.logging.Logging
import Credentials.fetchKeyFromParameterStore

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
import scala.jdk.CollectionConverters._

class ApiGatewayResponse(
  @BeanProperty var statusCode: Int,
  @BeanProperty var headers: java.util.Map[String, String],
  @BeanProperty var body: String
)

object ApiGatewayResponse {
  def apply(statusCode: Int, headers: Map[String, String], body: String): ApiGatewayResponse = {
    new ApiGatewayResponse(statusCode, headers.asJava, body)
  }
}

class ApiGatewayRequest {
  @BeanProperty var httpMethod: String = _
  @BeanProperty var path: String = _
  @BeanProperty var queryStringParameters: java.util.Map[String, String] = _
  @BeanProperty var headers: java.util.Map[String, String] = _
  @BeanProperty var body: String = _
  @BeanProperty var base64Encoded: Boolean = false
  @BeanProperty var stageVariables: java.util.Map[String, String] = _
  @BeanProperty var requestContext: ApiGatewayRequestContext = _

  private def asScalaMap[K, V](m: java.util.Map[K, V]): Map[K, V] = Option(m).map(_.asScala.toMap).getOrElse(Map.empty)
  def queryStringParamMap: Map[String, String] = asScalaMap(queryStringParameters)
  def headerMap: Map[String, String] = asScalaMap(headers)

  override def toString = s"ApiGatewayRequest(" +
    s"httpMethod = $httpMethod, " +
    s"path = $path, " +
    s"queryStringParameters = $queryStringParamMap, " +
    s"headers = $headerMap, " +
    s"base64Encoded = $base64Encoded, " +
    s"stageVariables = $stageVariables, " +
    s"requestContext = $requestContext  " +
    s"body = $body)"
}

class ApiGatewayRequestContext {
  @BeanProperty var stage: String = _

  override def toString = s"ApiGatewayRequestContext(stage = $stage)"
}

object Lambda extends Logging {
//
//  val googleSearchService: GoogleSearchService = {
//    val apiKey = fetchKeyFromParameterStore("Google/CustomSearch/ApiKey")
//    new GoogleSearchService(apiKey)
//  }

  /*
   * Logic handler
   */
  def go(capiId: Option[String]): String = {
    val eventual = Future {
      s"Hi there! $capiId"
    }

    Await.result(eventual, 40.seconds)
  }

  /*
   * Lambda's entry point
   */
  def handler(request: ApiGatewayRequest): ApiGatewayResponse = {
    ApiGatewayResponse(200, Map.empty, go(request.queryStringParamMap.get("capi-id")))
  }

}
