package com.theguardian.content.rules.model

case class Rule(criteriaByName: Map[String, String], recommendation: String) {
  def isSatisfiedByContext(context: Context): Boolean = {
    val content = context.content

    criteriaByName.get("if-in-section").forall(requiredSection => content.section.exists(_.id == requiredSection))
  }

}

object Rule {
  def from(headerRow: Seq[String], ruleRow: Seq[String]): Option[Rule] = {
    val valuesByName = headerRow.zip(ruleRow).toMap.filter(! _._2.isBlank)
    println(valuesByName)
    for {
      recommendation <- valuesByName.get("recommendation")
    } yield Rule(valuesByName, recommendation)
  }
}