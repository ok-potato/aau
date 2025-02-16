package at.jku.risc.stout.aau.term;

import at.jku.risc.stout.aau.impl.Algorithm;

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
    public MappedVariableTerm(String head) {
        super(head, Collections.emptyList());
    }
    
    @Override
    public String toString() {
        return head();
    }
}
