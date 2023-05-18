package com.theguardian.content.rules

import com.theguardian.content.rules.model.{Context, Rule}

case class RuleEngine(rules: Seq[Rule]) {
  def findRecommendations(context: Context, maxRecommendations: Int = 1): Seq[String] =
    rules.filter(_.isSatisfiedByContext(context)).map(_.recommendation).distinct.take(maxRecommendations)
}

object RuleEngine {
  def fromGrid(cells: Seq[Seq[String]]): RuleEngine = {
    val headerRow = cells.head
    val rulesRows = cells.tail

    RuleEngine(rulesRows.flatMap(ruleRow => Rule.from(headerRow, ruleRow)))
  }
}
