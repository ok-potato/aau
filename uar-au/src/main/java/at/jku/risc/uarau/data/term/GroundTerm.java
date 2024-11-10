package at.jku.risc.uarau.data.term;

import at.jku.risc.uarau.util.Panic;
import at.jku.risc.uarau.util.Util;
import org.junit.platform.commons.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * {@linkplain GroundTerm} is the basic encoding for the terms in the problem statement.
 * <br><br>
 * Given some problem domain with function terms, the function terms can be mapped to {@linkplain GroundTerm GroundTerms}.
 * <br>
 * If the problem domain also contains variable terms, those can be encoded as {@linkplain MappedVariableTerm MappedVariableTerms}.
 * <br>
 * Each function/variable symbols must be representable by a String (which cannot contain '(', ')' or ',').
 * <br><br>
 * Alternatively, the whole equation can be supplied as a String
 * (see {@linkplain at.jku.risc.uarau.Problem#Problem(String) Problem(String)})
 */
public class GroundTerm implements Term {
    public final String head;
    public final List<GroundTerm> arguments;
    
    public GroundTerm(String head, List<GroundTerm> arguments) {
        if (StringUtils.isBlank(head) || head.contains("(") || head.contains(")") || head.contains(",")) {
            throw Panic.arg("Illegal head '%s'", head);
        }
        this.head = head.intern();
        this.arguments = Collections.unmodifiableList(arguments);
    }
    
    /**
     * During execution, this method is used to convert a term which is known to be ground to its properly typed
     * {@linkplain GroundTerm} representation.
     * <br>
     * This is essentially a convenient way to universally assert ground-ness of terms in
     * {@linkplain at.jku.risc.uarau.data.AUT AUTs} on the type level.
     * <br><br>
     * <b>Note:</b> this is also why ANON is a {@linkplain MappedVariableTerm} and not a {@linkplain VariableTerm}.
     * The latter representation is technically more correct, but not as useful.
     */
    public static GroundTerm force(Term term) {
        if (term instanceof GroundTerm) {
            return (GroundTerm) term;
        }
        if (!(term instanceof FunctionTerm)) {
            throw Panic.state("Couldn't cast sub-term '%s' of type %s", term, term.getClass());
        }
        FunctionTerm functionTerm = (FunctionTerm) term;
        return new GroundTerm(functionTerm.head, Util.mapList(functionTerm.arguments, GroundTerm::force));
    }
    
    @Override
    public Set<Integer> v_named() {
        return Collections.emptySet();
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
        if (!(other instanceof GroundTerm)) {
            return false;
        }
        if (this == MappedVariableTerm.ANON || other == MappedVariableTerm.ANON) {
            return false;
        }
        GroundTerm otherFunctionTerm = (GroundTerm) other;
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
