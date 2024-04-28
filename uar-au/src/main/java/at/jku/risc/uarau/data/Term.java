package at.jku.risc.uarau.data;

import org.junit.platform.commons.util.StringUtils;

import java.util.Stack;

public abstract class Term {
    public final String head;
    
    public Term(String head) {
        if (StringUtils.isBlank(head)) {
            throw new IllegalArgumentException("https://youtu.be/aS8O-F0ICxw");
        }
        this.head = head;
    }
    
    // terms look like this:  'f(g(a, b), c, d)'
    // you can write function/variable names using any symbols besides '(),'
    // whitespace is ignored
    
    public static Term parse(String term) {
        // 'f(g(a, b), c, d)'  =>  'f(', 'g(', 'a', 'b', ')', 'c', 'd', ')'
        String[] tokens = term.replaceAll("\\s", "").split("((?<=\\()|,|(?=\\)))");
        Stack<Function> fTerms = new Stack<>();
        fTerms.add(new Function(",")); // "dummy term" -> avoid null pointer
        for (String s : tokens) {
            try {
                if (")".equals(s)) { // end of a set of arguments
                    Function subTerm = fTerms.pop();
                    fTerms.peek().arguments.add(subTerm);
                    continue;
                }
                if (s.endsWith("(")) { // head of a function, beginning of a set of arguments
                    fTerms.add(new Function(s.substring(0, s.length() - 1)));
                    continue;
                }
                fTerms.peek().arguments.add(new Variable(s)); // just a variable
            } catch (Exception e) {
                String msg = STR."Error while parsing term '\{term}' at token '\{s}', check syntax!";
                if (")".equals(s)) {
                    msg += " (Too many closing parentheses)";
                }
                throw new IllegalArgumentException(msg);
            }
        }
        if (fTerms.size() != 1) {
            throw new IllegalArgumentException(STR."Term \{term} contains \{fTerms.size() - 1} unclosed parentheses!");
        }
        
        return fTerms.pop().arguments.getFirst(); // deref. "dummy term"
    }
}
