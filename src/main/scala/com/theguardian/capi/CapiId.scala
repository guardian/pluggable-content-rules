package com.theguardian.capi

import com.gu.contentapi.client.model.v1.Content
import upickle.default.*

case class CapiId(value: String) {
  require(value.nonEmpty, s"Capi id is empty")

  // the value _should_ be opaque, but as it's probably path-like, let's check we haven't got a
  // slash at the start of it, like much Ophan code tends to have.
  require(!value.startsWith("/"), s"Capi id must not start with a forward slash: $value")
}

object CapiId {
  implicit val capiIdRW: ReadWriter[CapiId] = readwriter[String].bimap[CapiId](_.value, CapiId(_))

  implicit class RichContent(content: Content) {
    val capiId: CapiId = CapiId(content.id)
  }
}