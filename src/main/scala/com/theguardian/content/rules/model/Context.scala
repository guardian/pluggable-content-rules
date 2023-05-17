package com.theguardian.content.rules.model

/** 
 * All info supplied that can be used to make a decision about what to show
 * */
case class Context(
  content: com.gu.contentapi.client.model.v1.Content,
  // edition: Edition
)