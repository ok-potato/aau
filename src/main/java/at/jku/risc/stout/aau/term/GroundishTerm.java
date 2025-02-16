package at.jku.risc.stout.aau.term;

import at.jku.risc.stout.aau.Witness;
import at.jku.risc.stout.aau.util.Data;
import at.jku.risc.stout.aau.util.Panic;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static at.jku.risc.stout.aau.term.Anon.ANON;

/**
 * {@linkplain GroundishTerm Groundish Terms} contain no variables as sub-terms besides ANON.
 * {@linkplain at.jku.risc.stout.aau.impl.AUT AUTs}, {@linkplain at.jku.risc.stout.aau.impl.Expression Expressions}
 * and the solution's {@linkplain Witness Witnesses} all have this property.
 */
public abstract class GroundishTerm implements Term {
    private final String head;

    protected GroundishTerm(String head) {
        this.head = head.intern();
    }

    @Override
    public String head() {
        return head;
    }

    public abstract List<? extends GroundishTerm> arguments();

    /**
     * During execution, this method is used to convert a term which is known to be ground aside from ANON
     * to a properly typed {@linkplain GroundishTerm}.
     */
    public static GroundishTerm force(Term term) {
        if (term instanceof GroundishTerm) {
            return (GroundishTerm) term;
        }
        if (!(term instanceof FunctionTerm)) {
            throw Panic.state("Couldn't cast sub-term '%s' of type %s", term, term.getClass());
        }
        FunctionTerm functionTerm = (FunctionTerm) term;
        return new GroundishTermImpl(functionTerm.head(), Data.mapToList(functionTerm.arguments(), GroundishTermImpl::force));
    }

    @Override
    public Set<Integer> namedVariables() {
        return Collections.emptySet();
    }

    private Integer hash = null;

    @Override
    public int hashCode() {
        if (hash == null) {
            hash = head().hashCode() + 31 * arguments().hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (this == ANON || other == ANON) {
            return false;
        }
        if (!(other instanceof GroundishTerm) || hashCode() != other.hashCode()) {
            return false;
        }
        GroundishTerm otherFunctionTerm = (GroundishTerm) other;
        return head() == otherFunctionTerm.head() && arguments().equals(otherFunctionTerm.arguments());
    }

    @Override
    public String toString() {
        if (this == ANON) {
            return "_";
        }
        return head() + Data.str(arguments(), ",", "()", "(", ")");
    }
}
