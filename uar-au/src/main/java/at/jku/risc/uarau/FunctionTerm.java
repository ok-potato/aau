package at.jku.risc.uarau;

import at.jku.risc.uarau.util.Util;
import org.junit.platform.commons.util.StringUtils;

import java.util.*;

public class FunctionTerm implements Term {
    public final String head;
    public final List<Term> arguments;
    
    // function/constant term
    public FunctionTerm(String head, List<Term> arguments) {
        if (StringUtils.isBlank(head) || head.contains("(") || head.contains(")") || head.contains(",")) {
            throw new IllegalArgumentException("Illegal head: " + head);
        }
        this.head = head.intern();
        this.arguments = Collections.unmodifiableList(arguments);
    }
    
    public FunctionTerm(String head, Term[] arguments) {
        this(head, Arrays.asList(arguments));
    }
    
    private Set<Integer> v_named = null;
    
    @Override
    public Set<Integer> v_named() {
        if (v_named == null) {
            v_named = new HashSet<>();
            for (Term argument : arguments) {
                v_named.addAll(argument.v_named());
            }
        }
        return v_named;
    }
    
    private Integer hash = null;
    
    @Override
    public int hashCode() {
        if (hash == null) {
            hash = head.hashCode() + 31 * arguments.hashCode();
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FunctionTerm)) {
            return false;
        }
        FunctionTerm otherFunctionTerm = (FunctionTerm) other;
        if (hashCode() != otherFunctionTerm.hashCode()) {
            return false;
        }
        return head == otherFunctionTerm.head && arguments.equals(otherFunctionTerm.arguments);
    }
    
    @Override
    public String toString() {
        return head + Util.str(arguments, ",", "()", "(", ")");
    }
}
