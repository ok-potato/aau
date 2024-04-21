package at.jku.risc.uarau.data;

public class Term {
    public static final Term ANON = new Term(-1, new Term[]{});
    private static int freshVar = -2;
    
    public final int head;
    public final Term[] arguments;
    
    private Term(int head, Term[] arguments) {
        this.head = head;
        this.arguments = arguments;
    }
    
    public static int freshVar() {
        return freshVar--;
    }
}
