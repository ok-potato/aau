package at.jku.risc.aau.impl;

import at.jku.risc.aau.term.GroundTerm;
import at.jku.risc.aau.util.ArraySet;
import at.jku.risc.aau.util.Data;

// TODO this is not clear
/**
 * {@linkplain Expression Expressions} are part of a {@linkplain State State's} description of a program state
 * during a {@linkplain Algorithm#doConjoin(ArraySet, int, boolean) conjunction}.
 * <br><br>
 * {@linkplain Expression#T} is the possible substitutions for the {@linkplain Expression#variable}.
 */
class Expression {
    final int variable;
    final ArraySet<GroundTerm> T;
    
    Expression(int variable, ArraySet<GroundTerm> T) {
        this.variable = variable;
        this.T = T;
    }
    
    @Override
    public String toString() {
        return String.format("%s in %s", variable, Data.str(T, ", ", "[]", "[", "]"));
    }
}
