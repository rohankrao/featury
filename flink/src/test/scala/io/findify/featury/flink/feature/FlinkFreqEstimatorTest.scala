package io.findify.featury.flink.feature

import io.findify.featury.features.FreqEstimatorSuite
import io.findify.featury.flink.{Featury, FlinkStreamTest}
import io.findify.featury.model.FeatureConfig.{FreqEstimatorConfig, PeriodicCounterConfig}
import io.findify.featury.model.Key.{Id, Tenant}
import io.findify.featury.model.Write.{PeriodicIncrement, PutFreqSample}
import io.findify.featury.model.{FeatureKey, FeatureValue, FrequencyValue, Key, PeriodicCounterValue, Schema, Write}
import org.apache.flink.api.scala._

import scala.concurrent.duration._

class FlinkFreqEstimatorTest extends FreqEstimatorSuite with FlinkStreamTest {
  val k = Key(config.ns, config.group, config.name, Tenant("1"), Id("x1"))

  override def write(values: List[PutFreqSample]): Option[FeatureValue] = {
    val conf = Schema(config.copy(refresh = 0.hour))
    Featury.process(env.fromCollection[Write](values), conf).executeAndCollect(100).lastOption
  }

}
