package com.theguardian.content.rules

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import Credentials.fetchKeyFromParameterStore
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.Collections
import scala.jdk.CollectionConverters._
object SpreadsheetService {

  val APPLICATION_NAME = "pluggable-content-rules"
  val JSON_FACTORY = GsonFactory.getDefaultInstance
  val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport

  import com.google.auth.oauth2.GoogleCredentials

  def getCredentials(): GoogleCredential = {
    val json = fetchKeyFromParameterStore("google-creds.json")

    GoogleCredential.fromStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
      .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));
  }
  val service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials()).setApplicationName(APPLICATION_NAME).build

  def getData(spreadsheetId: String): Seq[Seq[String]] = {
    val request = service.spreadsheets.get(spreadsheetId)
    request.setIncludeGridData(true)
    val response = request.execute
    val gridData = response.getSheets.get(0).getData.get(0)
    for {
      row <- gridData.getRowData.asScala.toSeq
    } yield for {
      col <- Option(row.getValues).toSeq.flatMap(_.asScala.toSeq)
    } yield col.getFormattedValue
  }


}
