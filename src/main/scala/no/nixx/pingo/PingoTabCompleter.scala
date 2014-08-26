package no.nixx.pingo

class PingoTabCompleter(val userInput: String, val completionCandidates: List[String]) {

  val matches = completionCandidates.filter(c => c.startsWith(userInput))
  var pos = 0

  def hasNext = {
    matches.nonEmpty
  }

  def next = {
    if (matches.isEmpty) {
      throw new IllegalArgumentException("No matches!")
    }

    if (pos == matches.size) {
      pos = 0
    }

    pos += 1
    matches(pos - 1)
  }
}