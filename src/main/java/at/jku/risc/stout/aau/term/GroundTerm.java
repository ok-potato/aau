package at.jku.risc.stout.aau.term;

import at.jku.risc.stout.aau.Problem;

import java.util.Collections;
import java.util.List;

/**
 * {@linkplain GroundTerm} is the basic encoding for the terms in the problem statement.
 * <br><br>
 * Given some problem domain with function terms, the function terms can be mapped to {@linkplain GroundTerm GroundTerms}.
 * <br>
 * If the problem domain also contains variable terms, those can be encoded as {@linkplain MappedVariableTerm MappedVariableTerms}.
 * <br>
 * Function heads/variable symbols are represented as a string.
 * <br><br>
 * Alternatively, the equation can be supplied as a string
 * (see {@linkplain Problem#Problem(String) Problem(String)}).
 */
public class GroundTerm extends GroundishTerm {
    private final List<GroundTerm> arguments;
    
    public GroundTerm(String head, List<GroundTerm> arguments) {
        super(head);
        this.arguments = Collections.unmodifiableList(arguments);
    }

    @Override
    public List<GroundTerm> arguments() {
        return arguments;
    }
}
