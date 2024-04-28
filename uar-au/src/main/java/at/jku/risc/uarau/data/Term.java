package at.jku.risc.uarau.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

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
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.size() - 1; i++) {
            sb.append(arguments.get(i)).append(",");
        }
        if (!arguments.isEmpty()) {
            sb.append(arguments.getLast());
        }
        return STR."\{head}(\{sb.toString()})";
    }
    
    // terms look like this:  'f(g(a, b), c, d)'
    // you can write function/variable names using any symbols besides '(),'
    // whitespace is ignored
    
    public static Term parse(String term) {
        // 'f(g(a, b), c, d)'  =>  'f(', 'g(', 'a', 'b', ')', 'c', 'd', ')'
        String[] tokens = term.replaceAll("\\s", "").split("((?<=\\()|,|(?=\\)))");
        Stack<Term> terms = new Stack<>();
        terms.add(Term.function(" ")); // "dummy term" -> avoid null pointer
        for (String s : tokens) {
            try {
                if (")".equals(s)) { // end of a set of arguments
                    Term subTerm = terms.pop();
                    terms.peek().arguments.add(subTerm);
                    continue;
                }
                if (s.endsWith("(")) { // head of a function, beginning of a set of arguments
                    terms.add(Term.function(s.substring(0, s.length() - 1)));
                    continue;
                }
                terms.peek().arguments.add(Term.variable(s)); // just a variable
            } catch (Exception e) {
                String msg = STR."Error while parsing term '\{term}' at token '\{s}', check syntax!";
                if (")".equals(s)) {
                    msg += " (Too many closing parentheses)";
                }
                throw new IllegalArgumentException(msg);
            }
        }
        if (terms.size() != 1) {
            throw new IllegalArgumentException(STR."Term \{term} contains \{terms.size() - 1} unclosed parentheses!");
        }
        
        return terms.pop().arguments.getFirst(); // deref. "dummy term"
    }
}
