package forex.cache.rates

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits.{catsSyntaxApplicativeError, catsSyntaxApplicativeId, catsSyntaxEitherId, toFlatMapOps, toFunctorOps}
import forex.cache.rates.Protocol.{RateCacheAPIEntry, RateCacheEntry}
import forex.cache.rates.errors.Error.{RateAPIQuotaReached, RateRetrieveFailed}
import forex.domain.Rate
import forex.domain.Rate.Pair
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax
import errors._
import org.http4s.{Header, Headers, Request}

import java.time.OffsetDateTime

class Program[F[_]: Sync](client: Client[F], container: Ref[F, Map[Pair, RateCacheEntry]], ttlSec: Long) extends Algebra[F, Pair, RateCacheEntry] {
  override def get(pair: Rate.Pair): F[Error Either RateCacheEntry] = {
    container.get.map(_.get(pair)).flatMap {
      case Some(rateCacheEntry) if rateCacheEntry.timestamp.value.isAfter(OffsetDateTime.now().minusSeconds(ttlSec)) =>
        println(s"Returning cached rate = ${rateCacheEntry}")
        rateCacheEntry.asRight[Error].pure[F]
      case _ => fetch(pair) flatMap {
        case Right(rateCacheEntry) => container.update(_ + (pair -> rateCacheEntry)).as(rateCacheEntry.asRight[Error])
        case Left(err) => err.asLeft[RateCacheEntry].pure[F]
      }
    }
  }

  def fetch(pair: Pair): F[Error Either RateCacheEntry] = {
    println(s"Fetching rate from API for pair ${pair.from}${pair.to}")
    val uri = uri"http://localhost:8081"
      .withPath("/rates")
      .withQueryParam("pair", s"${pair.from}${pair.to}")
    val request = Request[F](uri = uri, headers = Headers.of(Header("token", "my_token")))
    client.expect(request)(jsonOf[F, List[RateCacheAPIEntry]])
      .attempt
      .map {
        case Right(entryList) => Right(entryList.head.toRateCacheEntry)
        case Left(err) if err.getMessage.toLowerCase.contains("quota reached") => Left(RateAPIQuotaReached())
        case Left(err) => Left(RateRetrieveFailed(s"API returned error for pair ${pair.from}${pair.to}: ${err.getMessage}"))
      }
  }
}

object Program {

  def apply[F[_]: Sync](client: Client[F], ttlSec: Long): F[Algebra[F, Pair, RateCacheEntry]] = Ref.of[F, Map[Pair, RateCacheEntry]](Map.empty).map { container =>
    new Program[F](client, container, ttlSec)
  }

}
