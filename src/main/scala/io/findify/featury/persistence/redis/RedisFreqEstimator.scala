package io.findify.featury.persistence.redis

import cats.effect.IO
import io.findify.featury.feature.FreqEstimator
import io.findify.featury.feature.FreqEstimator.{FreqEstimatorConfig, FreqEstimatorState}
import io.findify.featury.model.Key
import redis.clients.jedis.Jedis

import scala.collection.JavaConverters._

class RedisFreqEstimator(val config: FreqEstimatorConfig, redis: Jedis) extends FreqEstimator {
  import KeyCodec._
  override def putReal(key: Key, value: String): IO[Unit] = {
    val multi = redis.multi()
    multi.lpush(key.toRedisKey("freq"), value)
    multi.ltrim(key.toRedisKey("freq"), 0, config.poolSize)
    IO { multi.exec() }
  }

  override def readState(key: Key): IO[FreqEstimator.FreqEstimatorState] = for {
    response <- IO { redis.lrange(key.toRedisKey("freq"), 0, -1) }
  } yield {
    FreqEstimatorState(response.asScala.toVector)
  }
}
