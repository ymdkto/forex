package forex

import scala.concurrent.ExecutionContext
import cats.effect._
import forex.cache.rates.{Program => CacheProgram}
import forex.config._
import fs2.Stream
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeClientBuilder[IO](executionContext).resource.use { client =>
      new Application[IO].stream(executionContext, client).compile.drain.as(ExitCode.Success)
    }
  }

}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream(ec: ExecutionContext, client: Client[F]): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      cache <- Stream.eval(CacheProgram[F](client, config.cache.ttlSec))
      module = new Module[F](config, cache)
      _ <- BlazeServerBuilder[F](ec)
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()

}
