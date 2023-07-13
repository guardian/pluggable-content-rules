package com.theguardian.content.rules.model

import com.gu.contentapi.client.model.v1.Content
import com.theguardian.capi.CapiId
import com.theguardian.capi.CapiId.*
import upickle.default.{ReadWriter => RW, macroRW}
import com.google.api.services.sheets.v4.model.Spreadsheet
case class ContentSummary(id: CapiId, webTitle: String)

object ContentSummary {
  def summaryFor(content: Content): ContentSummary = ContentSummary(content.capiId, content.webTitle)
}
case class SpreadsheetSummary(title: String)

object SpreadsheetSummary {
  def summaryFor(spreadsheet: Spreadsheet): SpreadsheetSummary =
    SpreadsheetSummary(spreadsheet.getProperties.getTitle)
}

object RecommendationResponse {
  given RW[ContentSummary] = macroRW
  given RW[SpreadsheetSummary] = macroRW
  given RW[RecommendationResponse] = macroRW
}

case class RecommendationResponse(
  content: Option[ContentSummary],
  spreadsheet: Option[SpreadsheetSummary],
  recommendations: Option[Seq[String]]
)