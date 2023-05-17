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
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}
import scala.jdk.FutureConverters.*
import scala.util.{Failure, Success}

object Lambda extends Logging {
//
//  val googleSearchService: GoogleSearchService = {
//    val apiKey = fetchKeyFromParameterStore("Google/CustomSearch/ApiKey")
//    new GoogleSearchService(apiKey)
//  }

  /*
   * Logic handler
   */
  def go(): Unit = {
    val eventual = Future {
      ???
    }

    Await.result(eventual, 40.seconds)
    println("Everything complete")
  }

  /*
   * Lambda's entry point
   */
  def handler(lambdaInput: ScheduledEvent, context: Context): Unit = {
    go()
  }

}
