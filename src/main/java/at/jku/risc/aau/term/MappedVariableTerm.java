package at.jku.risc.aau.term;

import at.jku.risc.aau.Solution;
import at.jku.risc.aau.impl.Algorithm;

import java.util.Collections;

/**
 * {@linkplain MappedVariableTerm} represent variables from the original problem domain.
 * <br>
 * From the perspective of the {@linkplain Algorithm Algorithm},
 * they act like constants - i.e. they are not substituted.
 * <br><br>
 * (See {@linkplain GroundTerm} for more details)
 */
public class MappedVariableTerm extends GroundTerm {
    /**
     * ANON (the anonymous variable) marks "irrelevant positions" in the
     * {@linkplain Solution#generalization Solution.generalizations}
     * <br>
     * i.e. positions where any term can serve as a substitution without effecting the generalization's proximity.
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
