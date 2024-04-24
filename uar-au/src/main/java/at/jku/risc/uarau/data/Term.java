package at.jku.risc.uarau.data;

import java.util.ArrayList;
import java.util.List;

public class Term {
    public static final Term ANONYMOUS = variable("_");
    
    public final String head;
    public final List<Term> arguments;
    public final boolean isVar;
    
    private Term(String head, List<Term> arguments, boolean isVar) {
        if (head == null || head.isEmpty()) {
            throw new IllegalArgumentException("https://youtu.be/aS8O-F0ICxw");
        }
        this.head = head;
        this.arguments = arguments;
        this.isVar = isVar;
    }
    
    public static Term function(String head) {
        return new Term(head, new ArrayList<>(), false);
    }
    
    public static Term variable(String head) {
        return new Term(head, List.of(), true);
    }
    
    @Override
    public String toString() {
        if (isVar) {
            return head;
        }
        
        StringBuilder sb = new StringBuilder(STR."\{head}(");
        for (int i = 0; i < arguments.size() - 1; i++) {
            sb.append(arguments.get(i)).append(",");
        }
        if (!arguments.isEmpty()) {
            sb.append(arguments.getLast());
        }
        return sb.append(")").toString();
    }
}
