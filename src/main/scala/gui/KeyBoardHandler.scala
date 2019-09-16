package gui

import akka.actor.ActorRef
import scalafx.scene.input.{KeyCode, KeyEvent}

class KeyBoardHandler(keyboardEventsReceiver: ActorRef) {
  def handle(keyEvent: KeyEvent) = keyEvent.code match {
    case KeyCode.Up => keyboardEventsReceiver ! "UP"
    case KeyCode.Down => keyboardEventsReceiver ! "DOWN"
    case KeyCode.Right => keyboardEventsReceiver ! "RIGHT"
    case KeyCode.Left => keyboardEventsReceiver ! "LEFT"
  }
}
