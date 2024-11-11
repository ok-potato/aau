package at.jku.risc.uarau.data;

import at.jku.risc.uarau.data.term.GroundTerm;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Util;

/**
 * {@linkplain Expression Expressions} are part of a {@linkplain State State's} description of a program state
 * during a {@linkplain at.jku.risc.uarau.Algorithm#doConjoin(ArraySet, int, boolean) conjunction}.
 * <br><br>
 * {@linkplain Expression#T} is the possible substitutions for the {@linkplain Expression#variable}.
 */
public class Expression {
    public final int variable;
    public final ArraySet<GroundTerm> T;
    
    public Expression(int variable, ArraySet<GroundTerm> T) {
        this.variable = variable;
        this.T = T;
    }
    
    @Override
    public String toString() {
        return String.format("%s in %s", variable, Util.str(T, ", ", "[]", "[", "]"));
    }
}
