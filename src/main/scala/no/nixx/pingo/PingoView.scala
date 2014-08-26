package no.nixx.pingo

import java.awt.{Event, Font, Dimension}
import java.awt.event.{KeyEvent, FocusEvent, KeyListener, FocusListener}
import java.util.Date
import javax.swing._

import no.nixx.pingo.OutputStream.OutputStream
import no.nixx.pingo.StringUtils.isPrintableChar

import scala.collection.mutable.ListBuffer

object Pingo {

  def main(args: Array[String]) {
    val frame = new JFrame("Pingo")
    frame.setPreferredSize(new Dimension(800, 600))
    frame.setIconImage(new ImageIcon("src/main/resources/pingo.png").getImage)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    val controller = new PingoController()
    val view = new PingoView(controller)
    controller.setView(view)

    val scrollableView = new JScrollPane(view)
    scrollableView.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)

    frame.add(scrollableView)
    frame.pack()
    frame.setVisible(true)

    view.redrawContent()
  }
}

class PingoView(val controller: Controller) extends JTextArea with View with KeyListener with FocusListener {

  val buffer = new ListBuffer[OutputLine]()
  val prompt = new StringBuilder("> ")
  val commandline = new StringBuilder()
  var tabCompleter: PingoTabCompleter = null

  setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12))
  addKeyListener(this)
  addFocusListener(this)

  override def output(line: String) {
    buffer.append(new OutputLine(OutputStream.Output, new Date(), line))
  }

  override def output(lines: List[String]) {
    lines.foreach(line => output(line))
  }

  override def error(line: String) {
    buffer.append(new OutputLine(OutputStream.Error, new Date(), line))
  }

  override def error(lines: List[String]) {
    lines.foreach(line => error(line))
  }

  override def keyPressed(e: KeyEvent) {
    val keyCode = e.getKeyCode
    val modifiers = e.getModifiers

    e.consume()

    // Content
    modifiers match {

      case Event.CTRL_MASK =>
        keyCode match {
          case KeyEvent.VK_L =>
            clearBuffer()
          case KeyEvent.VK_C =>
            clearCommandline()
          case _ =>
        }

      case Event.ALT_MASK =>
        keyCode match {
          case KeyEvent.VK_F4 =>
            shutdown()
          case _ =>
        }

      case _ =>
        // Tab completion
        keyCode match {
          case KeyEvent.VK_TAB =>
            if (tabCompleter == null) {
              tabCompleter = new PingoTabCompleter(getWordToComplete, controller.getCompletions(commandline.toString(), commandline.toString().length)) // TODO: Not sure about the second parameter, remove?
            }
            if (tabCompleter.hasNext) {
              setWordToComplete(tabCompleter.next)
            }
          case _ => tabCompleter = null
        }

        // Others
        keyCode match {
          case KeyEvent.VK_BACK_SPACE =>
            backspace()
          case KeyEvent.VK_ENTER =>
            executeCommand()
            clearCommandline()
          case _ =>
            addToCommandline(e.getKeyChar)
        }
    }

    redrawContent()

    // Caret
    modifiers match {
      case Event.CTRL_MASK =>
        keyCode match {
          case KeyEvent.VK_LEFT => // TODO
          case KeyEvent.VK_RIGHT => // TODO
          case _ =>
        }
      case _ =>
        keyCode match {
          case KeyEvent.VK_HOME => // TODO
          case KeyEvent.VK_END => // TODO
          case _ =>
        }
    }
  }

  def backspace() {
    if (commandline.nonEmpty) {
      commandline.deleteCharAt(commandline.length - 1)
    }
  }

  def executeCommand() {
    val commandlineAsString = commandline.toString()
    output(prompt.toString() + commandlineAsString)
    controller.executeCommand(commandlineAsString)
  }

  def getWordToComplete = {
    getWordFromEndPos(getText, getCaretPosition - 1)
  }

  def getWordFromEndPos(text: String, pos: Int): String = {
    if (pos < 0 || text(pos).isWhitespace) {
      ""
    } else {
      getWordFromEndPos(text, pos - 1) + text(pos)
    }
  }

  def setWordToComplete(newWord: String) {
    val oldWordAtCaret: String = getWordToComplete
    val replaceStart: Int = getCommandlineCaretPos - oldWordAtCaret.length
    val replaceEnd: Int = getCommandlineCaretPos
    val newCommandline: String = commandline.substring(0, replaceStart) + newWord + commandline.substring(replaceEnd)

    setCommandline(newCommandline)
  }

  override def keyTyped(e: KeyEvent) {
    e.consume()
  }

  override def keyReleased(e: KeyEvent) {
    e.consume()
  }

  override def focusGained(e: FocusEvent) {
  }

  override def focusLost(e: FocusEvent) {
  }

  def clearBuffer() {
    buffer.clear()
  }

  def addToCommandline(c: Char) {
    if (isPrintableChar(c)) {
      val commandlineCaretPosition = getCaretPosition - getCommandlineStartPos
      commandline.insert(commandlineCaretPosition, c)
    }
  }

  def setCommandline(str: String) {
    commandline.clear()
    commandline.append(str)
  }

  def getCommandlineCaretPos = {
    getCaretPosition - getCommandlineStartPos
  }

  def getCommandlineStartPos = {
    getText.length - commandline.length
  }

  def getCommandlineEndPos = {
    getText.length
  }

  def clearCommandline() {
    commandline.clear()
  }

  def shutdown() {
    controller.shutdown()
  }

  def redrawContent() {
    val sb = new StringBuilder()

    buffer.foreach(line => {
      sb ++= line.content
      sb ++= "\n"
    })
    sb ++= prompt
    sb ++= commandline

    val newText = sb.toString()
    setText(newText)
    setCaretPosition(newText.length)
  }
}

object OutputStream extends Enumeration {
  type OutputStream = Value
  val Output = Value("Output")
  val Error = Value("Error")
}

class OutputLine(val stream: OutputStream, val timestamp: Date, val content: String) {
}