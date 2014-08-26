package no.nixx.pingo

trait View {
  def output(line: String)
  def output(line: List[String])
  def error(line: String)
  def error(line: List[String])
}