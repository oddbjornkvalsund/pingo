package no.nixx.pingo

import java.awt.event.KeyEvent

object StringUtils {
  def isPrintableChar(c: Char) = {
    val block: Character.UnicodeBlock = Character.UnicodeBlock.of(c)
    (!Character.isISOControl(c)) &&
      c != KeyEvent.CHAR_UNDEFINED &&
      block != null &&
      block != Character.UnicodeBlock.SPECIALS
  }

}
