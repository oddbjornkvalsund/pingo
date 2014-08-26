package no.nixx.pingo

import java.awt._
import java.awt.event.{KeyEvent, KeyListener}
import javax.swing._

object NewGUI {
  def main(args: Array[String]) {
    val frame: JFrame = new JFrame("New GUI")
    val contentPane: Container = frame.getContentPane
    contentPane.setLayout(new GridBagLayout())

    val buffer: JTextArea = new JTextArea()
    buffer.setBackground(Color.GRAY)
    // buffer.setRows(0)

    val bufferScrollPane = new JScrollPane(buffer, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

    val cmd: JTextField = new JTextField()
    cmd.setBackground(Color.LIGHT_GRAY)

    cmd.addKeyListener(new KeyListener() {
      def keyTyped(e: KeyEvent) {

      }

      def keyPressed(e: KeyEvent) {
        if(e.getKeyCode == KeyEvent.VK_ENTER) {
          val newText: String = cmd.getText
          cmd.setText("")

          if(buffer.getText.isEmpty) {
            buffer.append(newText)
          } else {
            buffer.append("\n" + newText)
          }

          println("Preferred size before setRows: " + buffer.getPreferredSize)
          buffer.setRows(buffer.getText.split('\n').length) // Grow vertically
          bufferScrollPane.setPreferredSize(buffer.getPreferredSize)
          println("Preferred size after setRows: " + buffer.getPreferredSize)
          e.consume()
        }
      }

      def keyReleased(e: KeyEvent) {
      }
    })

    val bufferConstraints: GridBagConstraints = new GridBagConstraints()
    bufferConstraints.gridx = 0
    bufferConstraints.gridy = 0
    bufferConstraints.fill = GridBagConstraints.BOTH
    bufferConstraints.anchor = GridBagConstraints.PAGE_START
    bufferConstraints.weightx = 1.0
    bufferConstraints.weighty = 1.0
    contentPane.add(bufferScrollPane, bufferConstraints)
    // contentPane.add(buffer, bufferConstraints)

    val cmdConstraints: GridBagConstraints = new GridBagConstraints()
    cmdConstraints.gridx = 0
    cmdConstraints.gridy = 1
    cmdConstraints.fill = GridBagConstraints.HORIZONTAL
    cmdConstraints.anchor = GridBagConstraints.PAGE_START
    cmdConstraints.weightx = 1.0
    cmdConstraints.weighty = 0.0
    contentPane.add(cmd, cmdConstraints)

    val filler: JPanel = new JPanel()
    filler.setBackground(Color.WHITE)
    filler.setPreferredSize(new Dimension(1000, 1000 ))

    val fillerConstraints: GridBagConstraints = new GridBagConstraints()
    fillerConstraints.gridx = 0
    fillerConstraints.gridy = 2
    fillerConstraints.fill = GridBagConstraints.BOTH
    fillerConstraints.weightx = 1.0
    fillerConstraints.weighty = 1.0
    // contentPane.add(filler, fillerConstraints)

    frame.setPreferredSize(new Dimension(800, 600))
    frame.pack()
    frame.setVisible(true)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    cmd.requestFocusInWindow()
  }
}