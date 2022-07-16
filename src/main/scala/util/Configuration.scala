package util

import util.SinkStatus._

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.StreamsConfig

import java.util.Properties

object Configuration extends LazyLogging {

  private lazy val config: Config = {
    ConfigFactory
      .parseResources("local.conf")
      .withFallback(ConfigFactory.parseResources("default.conf"))
  }

  logger.info(s"Effective configuration: ${config.root.render}")

  lazy val env: String = config.getString("stream-environment")

  lazy val runningLocally: Boolean = env.startsWith("local")

  object Kafka {
    lazy val kafkaConfig: Config = config.getConfig("kafka")

    lazy val bootstrapServers: String = kafkaConfig.getString("bootstrap-servers")

    lazy val topicFlightStarts: String =
      kafkaConfig.getString("topic.events-start")
    lazy val topicFlightEnds: String = kafkaConfig.getString("topic.events-end")

    lazy val topicFlightDelayed: String =
      kafkaConfig.getString("topic.flight-delayed")

    lazy val groupIdPrefix: String = if (runningLocally) {
      s"$env-${System.getProperty("user.name")}"
    } else {
      kafkaConfig.getString("group-id-prefix")
    }

    lazy val sinkStatus: SinkStatus =
      kafkaConfig.getString("sink-status").toSinkStatus

    def consumerConfig[A](groupId: String, keySerde: Serde[A]): Properties = {
      val props = new Properties()
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, s"$groupIdPrefix-$groupId")
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed")
      props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, keySerde.getClass)

      props
    }
  }

  // TODO
  object ApiDb {}

  // TODO
  object InfluxDb {}
}
