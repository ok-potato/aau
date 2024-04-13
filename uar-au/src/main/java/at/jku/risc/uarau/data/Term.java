package at.jku.risc.uarau.data;

public abstract class Term {
    public final int head;
    public static final int ANON = -1;
    protected static int fresh = -2;
    
    public Term(int head) {
        this.head = head;
    }
}
