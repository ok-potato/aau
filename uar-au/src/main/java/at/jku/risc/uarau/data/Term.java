package at.jku.risc.uarau.data;

import org.junit.platform.commons.util.StringUtils;

import java.util.Stack;

public abstract class Term {
    public final String head;
    
    public Term(String head) {
        if (StringUtils.isBlank(head)) {
            throw new IllegalArgumentException("https://youtu.be/aS8O-F0ICxw");
        }
        this.head = head.intern();
    }
    
    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException("Inheritor of Term must implement equals!");
    }
    
    @Override
    public int hashCode() {
        System.err.println("hashCode was called on inheritor of 'Term' without custom implementation!");
        return super.hashCode();
    }
    
    // terms look like this: 'f(g(a,b),c,d)'
    // function/variable names can be written using any symbols, except '(', ')' or ','
    // whitespace is ignored
    public static Term parse(String term) {
        // 'f(g(a,b),c,d)'  =>  'f(', 'g(', 'a', 'b', ')', 'c', 'd', ')'
        String[] tokens = term.replaceAll("\\s", "").split("((?<=\\()|,|(?=\\)))");
        
        Stack<Function> functions = new Stack<>();
        functions.add(new Function(",")); // "dummy term" to avoid null pointer
        for (String s : tokens) {
            try {
                if (")".equals(s)) { // end of arguments for current function
                    Function f = functions.pop();
                    functions.peek().arguments.add(f);
                    continue;
                }
                if (s.endsWith("(")) { // head of a function -> start of arguments
                    functions.add(new Function(s.substring(0, s.length() - 1)));
                    continue;
                }
                functions.peek().arguments.add(new Variable(s)); // otherwise, it's just a variable
                //
            } catch (Exception e) {
                String msg = STR."Error while parsing term '\{term}' at token '\{s}', check syntax!";
                if (")".equals(s)) {
                    msg += " (Too many closing parentheses)";
                }
                throw new IllegalArgumentException(msg);
            }
        }
        if (functions.size() != 1) {
            throw new IllegalArgumentException(STR."Term \{term} contains \{functions.size() - 1} unclosed parentheses!");
        }
        
        return functions.pop().arguments.getFirst(); // dereference "dummy term"
    }
}
