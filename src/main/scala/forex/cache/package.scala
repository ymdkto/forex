package forex

import forex.cache.rates.Algebra
import forex.cache.rates.Protocol.RateCacheEntry
import forex.domain.Rate.Pair

package object cache {
  type RateCache[F[_]] = Algebra[F, Pair, RateCacheEntry]
}
