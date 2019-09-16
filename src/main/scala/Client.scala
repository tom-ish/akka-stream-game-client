import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import domain.{Player, Position}
import spray.json.DefaultJsonProtocol._

class Client(playerName: String)(implicit val actorSystem: ActorSystem, actorMaterializer: ActorMaterializer) {
  import spray.json._
  implicit val positionFormat = jsonFormat2(Position)
  implicit val playerFormat = jsonFormat2(Player)

  val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(s"ws://localhost:8080/?playerName=$playerName")).collect {
    case TextMessage.Strict(txt) => txt.parseJson.convertTo[List[Player]]
  }

  def run[A, B](input: Source[String, A], output: Sink[List[Player], B]) = {
       input.map(direction => TextMessage(direction))
        .viaMat(webSocketFlow)(Keep.both) // keep the materialized Future[WebSocketUpgradeResponse]
        .toMat(output)(Keep.both) // also keep the Future[Done]
        .run()
  }
}
