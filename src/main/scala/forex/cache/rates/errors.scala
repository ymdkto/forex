package forex.cache.rates

object errors {

  sealed trait Error
  object Error {
    final case class RateAPIQuotaReached() extends Error
    final case class RateRetrieveFailed(msg: String) extends Error
  }
}
