import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.{FunSuite, Matchers}
import scalafx.scene.input.{KeyCode, KeyEvent}
import spray.json.DefaultJsonProtocol._

class ClientTestSpec extends FunSuite with Matchers {
  test("should be able to login player") {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    // emitted when the stream completes
    val testSink = TestSink.probe[Message]

    // send this as a message over the WebSocket
    val outgoing = Source.empty[Message]

    // flow to use (note: not re-usable!)
    val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest("ws://localhost:8080/?playerName=tomo"))

    val (upgradeResponse, testProbe) =
    outgoing
      .viaMat(webSocketFlow)(Keep.right) // keep the materialized Future[WebSocketUpgradeResponse]
      .toMat(testSink)(Keep.both) // also keep the Future[Done]
      .run()

    testProbe.request(1)
    val msg = testProbe.expectNext(TextMessage("[{\"name\":\"tomo\",\"position\":{\"x\":0,\"y\":0}}]"))
    println(msg)
  }

  test("should be able to move player") {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val client = new Client("tomo")
    val input = Source.actorRef[String](5, OverflowStrategy.dropNew)
    val output = TestSink.probe[List[Player]]
    val ((inputMat,rslt), outputMat) = client.run(input, output)

    inputMat ! "up"

    outputMat.request(2)
    outputMat.expectNext(List(Player("tomo", Position(0, 0))))
    outputMat.expectNext(List(Player("tomo", Position(0, 1))))
  }
}


object Main {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem = ActorSystem()
    implicit val actorMaterializer = ActorMaterializer()

    val client = Client("Tomo")

    new KeyBoardHandler()
  }
}

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

case class Player(name: String, position: Position)
case class Position(x: Int, y: Int)

class KeyBoardHandler(keyboardEventsReceiver: ActorRef) {
  def handle(keyEvent: KeyEvent) = {
    case KeyCode.Up => keyboardEventsReceiver ! "up"
    case KeyCode.Down => keyboardEventsReceiver ! "down"
    case KeyCode.Right => keyboardEventsReceiver ! "right"
    case KeyCode.Left => keyboardEventsReceiver ! "left"
  }
}