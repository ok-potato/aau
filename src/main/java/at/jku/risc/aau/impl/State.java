package at.jku.risc.aau.impl;

import at.jku.risc.aau.term.GroundTerm;
import at.jku.risc.aau.util.ArraySet;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * {@linkplain State States} are mutable representations of the branching program states during a
 * {@linkplain Algorithm#doConjoin(ArraySet, int, boolean) conjunction}, where:
 * <ul>
 *     <li> {@linkplain State#s} is the substitutions needed to arrive at the state
 *     <li> {@linkplain State#expressions} is the remaining set of sub-terms to be reduced
 * </ul>
 * Similarly to {@linkplain Config}, when branching, a new {@linkplain State#copy()} is created per branch.
 * <br><br>
 * A {@linkplain State} with no remaining {@linkplain Expression Expressions} is a success state.
 */
class State {
    public final Queue<Substitution> s;
    public final Queue<Expression> expressions;
    
    private int freshVar;
    
    public State(ArraySet<GroundTerm> T, int freshVar) {
        this.expressions = new ArrayDeque<>();
        this.s = new ArrayDeque<>();
        this.freshVar = freshVar;
        expressions.add(new Expression(freshVar(), T));
    }
    
    private State(State original) {
        this.expressions = new ArrayDeque<>(original.expressions);
        this.s = new ArrayDeque<>(original.s);
        this.freshVar = original.freshVar;
    }
    
    public State copy() {
        return new State(this);
    }
    
    public int freshVar() {
        return freshVar++;
    }
    
    public int peekVar() {
        return freshVar;
    }
    
    @Override
    public String toString() {
        return expressions.toString();
    }
}
