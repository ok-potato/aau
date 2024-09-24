package at.jku.risc.uarau.util;

public class Pair<LEFT, RIGHT> {
    public final LEFT left;
    public final RIGHT right;
    
    public Pair(LEFT left, RIGHT right) {
        this.left = left;
        this.right = right;
    }
}