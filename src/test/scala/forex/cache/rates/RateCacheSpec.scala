package forex.cache.rates

import cats.effect.{IO, Sync}
import cats.Applicative
import cats.effect.concurrent.Ref
import forex.cache.rates.errors._
import forex.cache.rates.Protocol.RateCacheEntry
import forex.domain.{Currency, Price, Timestamp}
import forex.domain.Rate.Pair
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class RateCacheSpec extends AnyPropSpec with ScalaCheckDrivenPropertyChecks with Matchers {
  implicit val arbPair: Arbitrary[Pair] = Arbitrary(for {
    from <- Gen.oneOf("USD", "EUR", "GBP")
    to <- Gen.oneOf("USD", "EUR", "GBP") if from != to
  } yield Pair(Currency.fromString(from), Currency.fromString(to)))

  class ProgramWithMockedFetch[F[_]: Sync](container: Ref[F, Map[Pair, RateCacheEntry]]) extends Program[F](null, container, 1) {
    override def fetch(pair: Pair): F[Error Either RateCacheEntry] = Applicative[F].pure(Right(RateCacheEntry(pair.from, pair.to, Price(1.0), Price(1.0), Price(1.0), Timestamp.now)))
  }

  property("get") {
    Ref.of[IO, Map[Pair, RateCacheEntry]](Map.empty).map { container =>
      val program = new ProgramWithMockedFetch[IO](container)
      forAll { (pair: Pair) =>
        val result = program.get(pair).unsafeRunSync().getOrElse(null)
        val cached = container.get.unsafeRunSync().getOrElse(pair, null)
        result shouldBe cached
      }
    }.unsafeRunSync()
  }
}
