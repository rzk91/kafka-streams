package code

final case class FlightTimes(
  timestamp: Long,
  flightId: String,
  startId: Int,
  destinationId: Int,
  started: Boolean
)
