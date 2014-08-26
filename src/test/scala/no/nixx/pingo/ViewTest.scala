package no.nixx.pingo

class ViewTest extends PingoTestCase {

  "A ViewImpl" should "do nothing" in {
    val vi = new ViewImpl()
    vi.output("Foo")
    assert(true)
  }

}

class ViewImpl extends View {
  def output(line: String) {

  }

  def output(line: List[String]) {

  }

  def error(line: String) {

  }

  def error(line: List[String]) {

  }
}