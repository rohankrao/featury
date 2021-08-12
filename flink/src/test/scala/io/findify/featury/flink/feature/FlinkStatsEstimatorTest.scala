package io.findify.featury.flink.feature

import io.findify.featury.features.StatsEstimatorSuite
import io.findify.featury.flink.{Featury, FlinkStreamTest}
import io.findify.featury.model.FeatureConfig.{FreqEstimatorConfig, StatsEstimatorConfig}
import io.findify.featury.model.{FeatureKey, FeatureValue, FrequencyValue, Key, NumStatsValue, Schema, Write}
import io.findify.featury.model.Key.{Id, Tenant}
import io.findify.featury.model.Write.{PutFreqSample, PutStatSample}
import io.findify.flinkadt.api._

import scala.concurrent.duration._

class FlinkStatsEstimatorTest extends StatsEstimatorSuite with FlinkStreamTest {
  val k = Key(config.ns, config.scope, config.name, Tenant("1"), Id("x1"))

  override def write(values: List[PutStatSample]): Option[FeatureValue] = {
    val conf = Schema(config.copy(refresh = 0.hour))
    Featury.process(env.fromCollection[Write](values), conf).executeAndCollect(100).lastOption
  }

}
