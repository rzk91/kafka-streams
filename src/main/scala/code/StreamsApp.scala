package code

import util.Configuration.Kafka
import util.Configuration.runningLocally

import com.typesafe.scalalogging.LazyLogging
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._

import java.util.Properties

object StreamsApp extends LazyLogging {

  implicit def serde[A >: Null: Decoder: Encoder]: Serde[A] = {
    val serializer = (a: A) => a.asJson.noSpaces.getBytes
    val deserializer = (aAsBytes: Array[Byte]) => {
      val aAsString = new String(aAsBytes)
      val aOrError = decode[A](aAsString)
      aOrError match {
        case Right(value) => Option(value)
        case Left(error) =>
          logger.debug(s"Error while converting message $aOrError", error)
          Option.empty
      }
    }
    Serdes.fromFn[A](serializer, deserializer)
  }

  def main(args: Array[String]): Unit = {
    val builder = new StreamsBuilder
    val flightStartStream =
      builder.stream[String, FlightTimes](Kafka.topicFlightStarts)

    analyseFlightTimes(flightStartStream)
      .to(Kafka.topicFlightDelayed)

    val topology = builder.build()
    logger.info(s"${topology.describe()}")

    val application = new KafkaStreams(
      topology,
      Kafka.consumerConfig("Flight Status", stringSerde)
    )

    if (runningLocally) {
      // For testing purposes
      application.cleanUp()
    }

    application.start()

    sys.ShutdownHookThread {
      application.close()
    }
  }

  def analyseFlightTimes(
    startStream: KStream[String, FlightTimes]
  ): KStream[String, FlightDelayed] =
    startStream
      .selectKey((_, times) => times.flightId)
      .mapValues(Option(_))
      .peek {
        case (key, times) =>
          logger.trace(s"Incoming event: key = $key, flight times = $times")
      }
      .flatMapValues {
        case Some(times) if times.startId != 1 =>
          Some(FlightDelayed(times.flightId, true))
        case Some(times) => Some(FlightDelayed(times.flightId, false))
        case _           => None
      }
      .peek {
        case (key, delayed) =>
          logger.trace(s"Outgoing event: key = $key, flight delayed = $delayed")
      }
}
