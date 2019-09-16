package gui

import akka.actor.ActorRef
import scalafx.scene.input.{KeyCode, KeyEvent}

class KeyBoardHandler(keyboardEventsReceiver: ActorRef) {
  def handle(keyEvent: KeyEvent) = keyEvent.code match {
    case KeyCode.Up => keyboardEventsReceiver ! "up"
    case KeyCode.Down => keyboardEventsReceiver ! "down"
    case KeyCode.Right => keyboardEventsReceiver ! "right"
    case KeyCode.Left => keyboardEventsReceiver ! "left"
  }
}
