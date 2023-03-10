package forex.cache.rates

import forex.domain.{Currency, Price, Timestamp}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

import java.time.OffsetDateTime


object Protocol {

  implicit val rateCacheAPIEntryDecoder: Decoder[RateCacheAPIEntry] = deriveDecoder

  final case class RateCacheAPIEntry(from: String,
                                     to: String,
                                     bid: Double,
                                     ask: Double,
                                     price: Double,
                                     time_stamp: String) {
    def toRateCacheEntry: RateCacheEntry = RateCacheEntry(
      Currency.fromString(from),
      Currency.fromString(to),
      Price(bid),
      Price(ask),
      Price(price),
      Timestamp(OffsetDateTime.parse(time_stamp))
    )
  }

  final case class APIError(message: String)

  final case class RateCacheEntry(from: Currency,
                                  to: Currency,
                                  bid: Price,
                                  ask: Price,
                                  price: Price,
                                  timestamp: Timestamp)

}
