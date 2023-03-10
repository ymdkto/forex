package forex.services.rates

import cats.Applicative
import forex.cache.RateCache
import interpreters._

object Interpreters {
  def liveRate[F[_]: Applicative](cache: RateCache[F]): Algebra[F] = new LiveRateService[F](cache)
}
