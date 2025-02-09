package at.jku.risc.aau.term;

import at.jku.risc.aau.impl.Substitution;
import at.jku.risc.aau.util.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@linkplain FunctionTerm FunctionTerms} are generated via {@linkplain Substitution#apply(Term) substitutions}.
 * <br><br>
 * See {@linkplain GroundTerm} for representing function terms in the problem statement.
 */
public class FunctionTerm implements Term {
    private final String head;
    private final List<Term> arguments;
    
    public FunctionTerm(String head, List<Term> arguments) {
        this.head = head.intern();
        this.arguments = Collections.unmodifiableList(arguments);
    }
    
    private Set<Integer> v_named = null;

    public String head() {
        return head;
    }

    public List<Term> arguments() {
        return arguments;
    }

    @Override
    public Set<Integer> namedVariables() {
        if (v_named == null) {
            v_named = new HashSet<>();
            for (Term argument : arguments) {
                v_named.addAll(argument.namedVariables());
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
        return head + Data.str(arguments, ",", "()", "(", ")");
    }
}
