import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.testkit.scaladsl.TestSink
import domain.{Player, Position}
import gui.{Display, KeyBoardHandler}
import org.scalatest.{FunSuite, Matchers}
import scalafx.application
import scalafx.application.{JFXApp, Platform}
import scalafx.scene.Scene
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.layout.{AnchorPane, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scalafx.scene.text.Text
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn

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
    outputMat.expectNext(List(domain.Player("tomo", Position(0, 0))))
    outputMat.expectNext(List(domain.Player("tomo", Position(0, 1))))
  }
}













