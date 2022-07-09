package util

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.apache.kafka.common.serialization.Serde
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.scala.serialization.Serdes

object Implicits extends LazyLogging {

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
}
