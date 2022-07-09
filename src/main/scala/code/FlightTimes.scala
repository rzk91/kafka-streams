package code

final case class FlightTimes(
  timestamp: Long,
  flightId: Int,
  startId: Int,
  destinationId: Int,
  started: Boolean
)
