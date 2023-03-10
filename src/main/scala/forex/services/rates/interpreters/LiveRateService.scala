package forex.services.rates.interpreters

import cats.Applicative
import cats.data.EitherT
import cats.implicits.toFunctorOps
import forex.cache.RateCache
import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.services.rates.Algebra
import forex.services.rates.errors._

class LiveRateService[F[_]: Applicative](cache: RateCache[F]) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    EitherT(cache.get(pair).map {
      case Right(rateCacheEntry) => Right(Rate(Pair(rateCacheEntry.from, rateCacheEntry.to), rateCacheEntry.price, rateCacheEntry.timestamp))
      case Left(e) => Left(toServiceError(e))
    }).value
  }

}
