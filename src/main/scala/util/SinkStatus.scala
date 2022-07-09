package util

object SinkStatus extends Enumeration {
  type SinkStatus = Value
  val ACTIVE, DEBUG, TESTING, DISABLED = Value

  implicit class SinkStatusOps(val status: String) extends AnyVal {
      def toSinkStatus = withName(status.toUpperCase)
  }
}
