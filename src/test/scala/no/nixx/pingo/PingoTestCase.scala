package no.nixx.pingo

import org.scalatest._

abstract class PingoTestCase extends FlatSpec with Matchers with OptionValues with Inside with Inspectors {
}