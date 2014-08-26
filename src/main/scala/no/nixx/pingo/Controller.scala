package no.nixx.pingo

trait Controller {
  def getCompletions(commandLine: String, caretPosition: Int): List[String]
  def executeCommand(commandLine: String)
  def cancelExecutingCommand()
  def shutdown()
}
