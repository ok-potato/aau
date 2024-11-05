package at.jku.risc.uarau.data.term;

import at.jku.risc.uarau.util.Except;
import at.jku.risc.uarau.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GroundTerm extends FunctionTerm<GroundTerm> {
    
    public GroundTerm(String head, List<GroundTerm> arguments) {
        super(head, arguments);
    }
    
    public static GroundTerm force(Term term) {
        if (term instanceof GroundTerm) {
            return (GroundTerm) term;
        }
        if (!(term instanceof FunctionTerm<?>)) {
            throw Except.state("Couldn't cast sub-term '%s' of type %s", term, term.getClass());
        }
        FunctionTerm<?> functionTerm = (FunctionTerm<?>) term;
        return new GroundTerm(functionTerm.head, Util.mapList(functionTerm.arguments, GroundTerm::force));
    }
    
    @Override
    public Set<Integer> v_named() {
        return Collections.emptySet();
    }
}
