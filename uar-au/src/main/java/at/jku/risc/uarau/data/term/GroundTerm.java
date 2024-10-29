package at.jku.risc.uarau.data.term;

import at.jku.risc.uarau.util.Util;
import org.junit.platform.commons.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GroundTerm implements Term {
    
    public final String head;
    public final List<GroundTerm> arguments;
    
    public GroundTerm(String head, List<GroundTerm> arguments) {
        if (StringUtils.isBlank(head) || head.contains("(") || head.contains(")") || head.contains(",")) {
            throw Util.except("Illegal head '%s'", head);
        }
        this.head = head.intern();
        this.arguments = Collections.unmodifiableList(arguments);
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
