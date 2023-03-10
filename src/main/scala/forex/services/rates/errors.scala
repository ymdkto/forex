package forex.services.rates

import forex.cache.rates.errors.{ Error => CacheError }

object errors {

  sealed trait Error
  object Error {
    final case class LiveRateLookupFailed(msg: String) extends Error
  }

  def toError(error: scala.Error): Error = Error.LiveRateLookupFailed(error.getMessage)

  def toServiceError(error: CacheError): Error = error match {
    case CacheError.RateAPIQuotaReached() => Error.LiveRateLookupFailed("API has reached quota")
    case CacheError.RateRetrieveFailed(msg) => Error.LiveRateLookupFailed(msg)
  }
}
