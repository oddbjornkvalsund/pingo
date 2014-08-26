package no.nixx.pingo;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.List;

public class TabTest extends JFrame implements KeyListener {

    private JavaTabCompleter currentCompleter = null;
    private final JTextArea textArea;

    public static void main(String[] args) {
        new TabTest("TabTest");
    }

    public TabTest(String name) {
        super(name);

        textArea = new JTextArea();
        textArea.addKeyListener(this);
        add(textArea);

        setPreferredSize(new Dimension(800, 600));
        setVisible(true);
        pack();
    }

    @Override
    public void keyTyped(KeyEvent e) {
//        e.consume();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        final int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_TAB:
                if (currentCompleter == null) {
                    currentCompleter = new JavaTabCompleter(getWordAtCaret());
                }
                if (currentCompleter.hasNext()) {
                    setWordAtCaret(currentCompleter.next());
                }
                e.consume();
                break;
            default:
                currentCompleter = null;
                break;
        }
    }

    private String getWordAtCaret() {
        final StringBuilder wordAtCaret = new StringBuilder();
        final String text = textArea.getText();

        for (int i = textArea.getCaretPosition() - 1; i > -1; i--) {
            final char c = text.charAt(i);
            if (Character.isSpaceChar(c) || c == '\n') {
                break;
            } else {
                wordAtCaret.insert(0, c);
            }
        }

        return wordAtCaret.toString();
    }

    private void setWordAtCaret(String newWord) {
        final String oldWordAtCaret = getWordAtCaret();

        final int replaceStart = textArea.getCaretPosition() - oldWordAtCaret.length();
        final int replaceEnd = textArea.getCaretPosition();
        final String newContent = textArea.getText().substring(0, replaceStart) + newWord + textArea.getText().substring(replaceEnd);

        textArea.setText(newContent);
        textArea.setCaretPosition(replaceStart + newWord.length());
    }

    @Override
    public void keyReleased(KeyEvent e) {
//        e.consume();
    }
}

class JavaTabCompleter {
    @SuppressWarnings("FieldCanBeLocal")
    private final List<String> completions = Arrays.asList("ls", "cd", "grep", "foobar", "fisketur", "fabian");

    private int currentIndex;
    private final List<String> matches;

    public JavaTabCompleter(final String userInput) {
        this.currentIndex = 0;
        this.matches = Lists.newArrayList(Collections2.filter(completions, new Predicate<String>() {
            @Override
            public boolean apply(String completionCandidate) {
                return StringUtils.isNotBlank(userInput) && StringUtils.isNotBlank(completionCandidate) && completionCandidate.startsWith(userInput);
            }
        }));
    }

    public boolean hasNext() {
        return !matches.isEmpty();
    }

    public String next() {
        if (matches.isEmpty()) {
            throw new IllegalStateException("No matches!");
        }

        if (currentIndex == matches.size()) {
            currentIndex = 0;
        }

        return matches.get(currentIndex++);
    }
}