package code

import util.Configuration.{runningLocally, Kafka}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.common.serialization.Serde

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
    val flightEndStream =
      builder.stream[String, FlightTimes](Kafka.topicFlightEnds)

    analyseFlightTimes(flightStartStream, flightEndStream)
      .to(Kafka.topicFlightDelayed)

    val topology = builder.build()
    logger.info(s"${topology.describe()}")

    val application = new KafkaStreams(
      topology,
      Kafka.consumerConfig("Flight Status", Serdes.intSerde)
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
    startStream: KStream[String, FlightTimes],
    endStream: KStream[String, FlightTimes]
  ): KStream[String, FlightTimes] =
    ???
}
