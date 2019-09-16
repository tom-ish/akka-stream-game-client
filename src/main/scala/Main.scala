import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, OverflowStrategy}
import gui.{Display, GUI, KeyBoardHandler}

import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem = ActorSystem()
    implicit val actorMaterializer = ActorMaterializer()

    val name = StdIn.readLine()

    val client = new Client(name)

    val display = new Display()
    val input = Source.actorRef[String](5, OverflowStrategy.dropNew)
    val output = display.sink

    val ((inputMat,rslt), outputMat) = client.run(input, output)
    val keyBoardHandler = new KeyBoardHandler(inputMat)

    new GUI(keyBoardHandler, display).main(args)
  }
}
