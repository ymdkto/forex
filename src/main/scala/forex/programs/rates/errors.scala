package forex.programs.rates

import forex.services.rates.errors.{Error => RatesServiceError}
import io.circe.{Encoder, Json}

object errors {

  sealed trait Error extends Exception
  object Error {
    final case class RateLookupFailed(msg: String) extends Error
  }

  implicit val RateLookupFailedEncoder: Encoder[Error.RateLookupFailed] =
    Encoder.instance[Error.RateLookupFailed] { error =>
      import io.circe.syntax._
      Json.obj(
        "error" -> error.msg.asJson
      )
    }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.LiveRateLookupFailed(msg) => Error.RateLookupFailed(msg)
  }
}
