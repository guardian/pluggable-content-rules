package com.theguardian.content.rules.model

case class Rule(criteriaByName: Map[String, String], recommendation: String) {
  def isSatisfiedByContext(context: Context): Boolean = {
    val content = context.content

    val inSection = criteriaByName.get("if-in-section").forall(requiredSection => content.sectionId.contains(requiredSection))
    val notInSection = criteriaByName.get("unless-in-section").forall(excludeSection => ! content.sectionId.contains(excludeSection))
    val hasTag = criteriaByName.get("if-has-any-tag-of").forall(requiredTags => content.tags.exists(tag => requiredTags.split(",").contains(tag.id)))

    inSection && notInSection && hasTag
  }

}

object Rule {
  def from(headerRow: Seq[String], ruleRow: Seq[String]): Option[Rule] = {
    val valuesByName = headerRow.zip(ruleRow).toMap.filter(! _._2.isBlank)
    for {
      recommendation <- valuesByName.get("recommendation")
    } yield Rule(valuesByName, recommendation)
  }
}