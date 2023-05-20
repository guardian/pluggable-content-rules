package com.theguardian

import com.github.blemale.scaffeine.Scaffeine

import scala.concurrent.duration.FiniteDuration

object Caching {
  extension [K, V](c: Scaffeine[K, V])
    def expireBasedOnValue[K1 <: K, V1 <: V](cacheTimeForValue: V1 => FiniteDuration): Scaffeine[K1, V1] =
      c.expireAfter[K1, V1](
        create = { case (_, v) => cacheTimeForValue(v) },
        update = { case (_, v, _) => cacheTimeForValue(v) },
        read = {  case (_, _, currentDuration) => currentDuration }
      ) // reading a record should not extend expiration time!

  def cachingMissingResults[K, T](whenMissing: FiniteDuration, whenPresent: FiniteDuration): Scaffeine[K, Option[T]] =
    Scaffeine().expireBasedOnValue[K, Option[T]](v => if (v.isDefined) whenPresent else whenMissing)

}
