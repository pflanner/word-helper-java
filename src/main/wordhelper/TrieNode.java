package wordhelper;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    private char letter;
    private boolean isWord;
    private final Map<Character, TrieNode> children;

    public TrieNode() {
        this.isWord = false;
        this.children = new HashMap<>();
    }

    public TrieNode(char letter) {
        this();
        this.letter = letter;
    }

    public boolean isWord() {
        return isWord;
    }

    public void setWord(boolean isWord) {
        this.isWord = isWord;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public Map<Character, TrieNode> getChildren() {
        return children;
    }

    public TrieNode addChild(char letter) {
        if (!children.containsKey(letter)) {
            children.put(letter, new TrieNode(letter));
        }
        return children.get(letter);
    }
}
