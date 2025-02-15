package at.jku.risc.aau.term;

import at.jku.risc.aau.Solution;
import at.jku.risc.aau.Witness;

import java.util.Set;

/**
 * Implemented by:
 * <pre>
 * ├ {@linkplain GroundishTerm}
 * │ ├ {@linkplain GroundTerm}
 * │ │ └ {@linkplain MappedVariableTerm}
 * │ ├ {@linkplain GroundishTermImpl}
 * │ └ {@linkplain Anon}
 * ├ {@linkplain FunctionTerm}
 * └ {@linkplain VariableTerm}
 * </pre>
 * GroundTerms can only contain other GroundTerms as arguments.
 * GroundishTermImpl can contain any GroundishTerms, and FunctionTerms can contain any Terms.
 * <br><br>
 * The input problem must be ground, so can only use GroundTerms.
 * The computed {@linkplain Solution#generalization generalizations} can contain variables, and is therefore of type Term.
 * The {@linkplain Witness Witnesses} can at most contain the anonymous variable, and are therefore of type GroundishTerm.
 */
public interface Term {
    String head();
    /**
     * The set of variables (excluding ANON) which appear as sub-terms in this term.
     */
    Set<Integer> namedVariables();
}
