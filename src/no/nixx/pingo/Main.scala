package no.nixx.pingo

import javax.swing._
import java.awt.{Font, Dimension}
import java.awt.event.{FocusEvent, FocusListener, KeyEvent, KeyListener}
import scala.collection.mutable.ArrayBuffer
import java.io.File

/**
 * Sane command shell for Windows.
 */
object Main {

  var cwd = new File(".").getAbsoluteFile.getParentFile
  var prompt = "> "
  val PINGO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12)

  def main(args: Array[String]) {
    val frame = new JFrame("Pingo")
    frame.setPreferredSize(new Dimension(800, 600))
    frame.setIconImage(new ImageIcon("res/pingo.png").getImage)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    val console = new PingoConsole()
    console.setFont(PINGO_FONT)

    frame.add(console)
    frame.pack()
    frame.setVisible(true)

    console.updateView()
  }
}

class PingoConsole extends JTextArea with KeyListener with FocusListener {

  var CTRL_IS_PRESSED = false
  var ALT_IS_PRESSED = false
  var SHIFT_IS_PRESSED = false

  val content = ArrayBuffer[String]()
  var prompt = "> "
  val commandLine = new StringBuilder

  addKeyListener(this)
  addFocusListener(this)

  def clearContent() = {
    content.clear()
  }

  def addLineToContent(line: String) = {
    content += line
  }

  def addLinesToContent(lines: List[String]) = {
    content ++= lines
  }

  def addCommandLineToContent() = {
    addLineToContent(prompt + commandLine.toString())
  }

  def clearCommandLine() = {
    commandLine.clear()
  }

  def addCharToCommandLine(c: Char) = {
    commandLine.append(c)
  }

  def backspace() {
    if (commandLine.length > 0) {
      commandLine.deleteCharAt(commandLine.length - 1)
    }
  }

  def updateView() = {
    val allContent = new StringBuilder

    content.foreach(line => {
      allContent ++= line
      allContent += '\n'
    })
    allContent ++= prompt
    allContent ++= commandLine

    setText(allContent.toString())
    setCaretPosition(getDocument.getLength)
  }

  def findAndCallCommandHandler(s: String) = {
    // Expand aliases
    // Expand variables
    // Parse into pipeline or
    if (s.startsWith("ls")) {
      val handler: lsCommandHandler = new lsCommandHandler
      handler.handle(s.split(" ").tail.toList, List.empty)
      addLinesToContent(handler.getOutput)
    } else if (s.startsWith("cd")) {
      val handler: cdCommandHandler = new cdCommandHandler
      handler.handle(s.split(" ").tail.toList, List.empty)
      addLinesToContent(handler.getOutput)
    }
  }

  def keyPressed(e: KeyEvent) {
    e.consume()

    val keycode = e.getKeyCode
    if (keycode == KeyEvent.VK_CONTROL) {
      CTRL_IS_PRESSED = true
    } else if (keycode == KeyEvent.VK_SHIFT) {
      SHIFT_IS_PRESSED = true
    } else if (keycode == KeyEvent.VK_ALT) {
      ALT_IS_PRESSED = true
    } else if (keycode == KeyEvent.VK_TAB) {
      // TODO
    } else if (keycode == KeyEvent.VK_ENTER) {
      addCommandLineToContent()
      findAndCallCommandHandler(commandLine.toString())
      clearCommandLine()
    } else if (keycode == KeyEvent.VK_BACK_SPACE) {
      backspace()
    } else {
      if (CTRL_IS_PRESSED) {
        keycode match {
          case KeyEvent.VK_C => clearCommandLine()
          case KeyEvent.VK_L => clearContent()
        }
      } else if (ALT_IS_PRESSED) {
        keycode match {
          case KeyEvent.VK_F4 => System.exit(0)
        }
      } else {
        if (isPrintableChar(e.getKeyChar)) {
          addCharToCommandLine(e.getKeyChar)
        }
      }
    }

    updateView()
  }

  def isPrintableChar(c: Char) = {
    val block: Character.UnicodeBlock = Character.UnicodeBlock.of(c)
    (!Character.isISOControl(c)) &&
      c != KeyEvent.CHAR_UNDEFINED &&
      block != null &&
      block != Character.UnicodeBlock.SPECIALS
  }

  def keyTyped(e: KeyEvent) {
    e.consume()
  }

  def keyReleased(e: KeyEvent) {
    e.getKeyCode match {
     case KeyEvent.VK_CONTROL => CTRL_IS_PRESSED = false
     case KeyEvent.VK_SHIFT => SHIFT_IS_PRESSED = false
     case KeyEvent.VK_ALT => ALT_IS_PRESSED = false
    }
  }

  def focusGained(e: FocusEvent) {
    CTRL_IS_PRESSED = false
    SHIFT_IS_PRESSED = false
    ALT_IS_PRESSED = false
  }

  def focusLost(e: FocusEvent) {
    CTRL_IS_PRESSED = false
    SHIFT_IS_PRESSED = false
    ALT_IS_PRESSED = false
  }
}

trait CommandHandler {
  def getCommandName: String

  def handle(args: List[String], input: List[String])

  def getExitStatus: Int

  def getOutput: List[String]
}

class lsCommandHandler extends CommandHandler {
  val output = ArrayBuffer[String]()

  def getCommandName: String = "ls"

  def handle(args: List[String], input: List[String]): Unit = {
    if (args.isEmpty) {
      output ++= getFilesNamesInDir(Main.cwd)
    } else {
      args.foreach(f => {
        val file: File = new File(Main.cwd, f)
        if (file.isDirectory) {
          output += (f + ":")
          output ++= getFilesNamesInDir(file)
          output += ""
        } else if (file.isFile) {
          output += f
        } else {
          System.err.println("No such file or directory: " + f)
        }
      })
    }
  }

  def getFilesNamesInDir(dir: File): List[String] = {
    dir.listFiles()
      .sortWith(
        (a, b) => a.getName.compareTo(b.getName) <= 0
      )
      .map(
        f => if (f.isDirectory) f.getName + "/" else f.getName
      ).toList
  }

  def getExitStatus: Int = 0

  def getOutput: List[String] = output.toList
}

class cdCommandHandler extends CommandHandler {

  // TODO: History: "cd -"
  // TODO: Use commons-io and commons-lang for file/directory handling

  val output = ArrayBuffer[String]()

  def getCommandName: String = "cd"

  def handle(args: List[String], input: List[String]): Unit = {
    if (args.length == 1) {
      val dir = args(0)
      if (isParentDir(dir)) {
        Main.cwd = Main.cwd.getAbsoluteFile.getParentFile
      } else {
        val file: File = if (isAbsolutePath(dir)) new File(dir) else new File(Main.cwd, dir)
        if (file.isDirectory) {
          Main.cwd = file
        } else {
          output += "No such directory: " + file
        }
      }
    } else {
      output += "cd takes only one argument"
    }
    println("Current working directory is: " + Main.cwd)
  }

  def isParentDir(s: String):Boolean = s.matches("^\\.\\.[\\/]*$")

  def isAbsolutePath(s: String): Boolean = s.matches("^\\w:.*")

  def getExitStatus: Int = 0

  def getOutput: List[String] = output.toList

}

class TabCompleter(val userInput: String, val completionCandidates: List[String]) {

  val matches = completionCandidates.filter(c => c.startsWith(userInput))
  var pos = 0

  def hasNext = {
    matches.nonEmpty
  }

  def next = {
    if(matches.isEmpty) {
      throw new IllegalArgumentException("No matches!")
    }

    if(pos == matches.size) {
      pos = 0
    }

    matches(pos++)
  }
}