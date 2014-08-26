package no.nixx.pingo

import java.lang.System.exit

class PingoController extends Controller {

  var view: View = null

  override def getCompletions(commandLine: String, caretPosition: Int): List[String] = {
    List("ls", "cd", "cls")
  }

  override def executeCommand(commandLine: String) {
  }

  override def cancelExecutingCommand() {
  }

  override def shutdown() {
    exit(0)
  }

  def setView(view: View) {
    this.view = view
  }
}