package forex.services.rates

import forex.domain._

object Protocol {

  final case class GetRatesResponse(
                                   from: Currency,
                                   to: Currency,
                                   bid: Price,
                                   ask: Price,
                                   price: Price,
                                   timestamp: Timestamp
                                 )

}
