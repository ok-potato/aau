package at.jku.risc.uarau.data.term;

import at.jku.risc.uarau.data.Solution;

import java.util.Collections;

/**
 * {@linkplain MappedVariableTerm} represent variables from the original problem domain.
 * <br>
 * (See {@linkplain GroundTerm} for more details)
 */
public class MappedVariableTerm extends GroundTerm {
    /**
     * ANON (the anonymous variable) marks "irrelevant positions" in the {@linkplain Solution} terms -
     * i.e. positions for which any term can serve as a substitution.
     */
    public static final MappedVariableTerm ANON = new MappedVariableTerm("_");
    
    public MappedVariableTerm(String head) {
        super(head, Collections.emptyList());
    }
    
    @Override
    public String toString() {
        return head;
    }
}
