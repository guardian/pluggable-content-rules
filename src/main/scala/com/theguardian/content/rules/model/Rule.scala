package com.theguardian.content.rules.model

case class Rule(criteriaByName: Map[String, String], recommendation: String) {
  def isSatisfiedByContext(context: Context): Boolean = {
    val content = context.content

    val inSection = criteriaByName.get("if-in-section").forall(requiredSection => content.sectionId.contains(requiredSection))
    val notInSection = criteriaByName.get("unless-in-section").forall(excludeSection => ! content.sectionId.contains(excludeSection))
    val hasTag = criteriaByName.get("if-has-any-tag-of").forall(requiredTags => content.tags.exists(tag => requiredTags.split(",").contains(tag.id)))
    println("Content section " + content.sectionId)
    println("Content tags " + content.tags.map(_.id).mkString(", "))
    println(s"inSection $inSection NotInSection $notInSection hasTag $hasTag Recommendation $recommendation")
    inSection &&
      notInSection &&
      hasTag
    // if-has-any-tag-of	if-is-edition	unless-is-edition	if-in-section	unless-in-section
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