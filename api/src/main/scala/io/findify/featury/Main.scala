package io.findify.featury

import org.http4s.HttpRoutes
import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe.syntax._
import cats.implicits._
import io.findify.featury.api.ValuesApi
import io.findify.featury.config.{ApiConfig, Args}
import io.findify.featury.connector.redis.RedisStore
import io.findify.featury.model.api.{ReadRequest, ReadResponse}
import io.findify.featury.util.ForkJoinExecutor
import io.findify.featury.values.{FeatureStore, MemoryStore}
import io.findify.featury.values.ValueStoreConfig.{MemoryConfig, RedisConfig}
import org.http4s.blaze.server._
import org.http4s.implicits._
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  implicit val requestDecoder: EntityDecoder[IO, ReadRequest]   = jsonOf
  implicit val responseEncoder: EntityEncoder[IO, ReadResponse] = jsonEncoderOf
  implicit lazy val ec: ExecutionContext                        = ForkJoinExecutor("api", 4)

  override def run(args: List[String]): IO[ExitCode] = for {
    logger  <- Slf4jLogger.create[IO]
    cmdline <- IO(Args.parse(args))
    config <- cmdline.configFile match {
      case Some(value) => logger.info(s"loading API config from $value") *> ApiConfig.fromFile(value)
      case None        => logger.info("loading default API config") *> IO(ApiConfig.default)
    }
    result <- config.store match {
      case redisConfig: RedisConfig =>
        logger.info("using Redis client") *>
          RedisStore
            .makeRedisClient(redisConfig)
            .use(redis => serve(config, RedisStore(redis, redisConfig.codec), logger))
      case _: MemoryConfig =>
        logger.info("using Memory to store feature values") *>
          Resource.make(IO(new MemoryStore()))(_ => IO.unit).use(mem => serve(config, mem, logger))
    }
  } yield {
    result
  }

  def serve(config: ApiConfig, store: FeatureStore, logger: Logger[IO])(implicit ec: ExecutionContext) = {
    val api     = ValuesApi(store, logger)
    val httpApp = Router("/" -> api.service).orNotFound
    logger.info("starting API service") *>
      BlazeServerBuilder[IO](ec)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
  }

}
