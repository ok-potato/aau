package at.jku.risc.uarau.data.term;

import at.jku.risc.uarau.util.Except;
import at.jku.risc.uarau.util.Util;
import org.junit.platform.commons.util.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Similar to {@linkplain VariableTerm}s, function terms are generated during the computation process,
 * but can't appear in problem definitions.
 */
public class FunctionTerm<T extends Term> implements Term {
    public final String head;
    public final List<T> arguments;
    
    public FunctionTerm(String head, List<T> arguments) {
        if (StringUtils.isBlank(head) || head.contains("(") || head.contains(")") || head.contains(",")) {
           throw Except.argument("Illegal head '%s'", head);
        }
        this.head = head.intern();
        this.arguments = Collections.unmodifiableList(arguments);
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
        if (!(other instanceof FunctionTerm<?>)) {
            return false;
        }
        if (this == MappedVariableTerm.ANON || other == MappedVariableTerm.ANON) {
            return false;
        }
        FunctionTerm<?> otherFunctionTerm = (FunctionTerm<?>) other;
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
