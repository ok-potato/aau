package at.jku.risc.uarau.data;

import at.jku.risc.uarau.GroundTerm;
import at.jku.risc.uarau.Substitution;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * mutable
 */
public class State {
    public final Queue<Expression> expressions;
    public final Queue<Substitution> s;
    
    private int freshVar;
    
    public State(Queue<GroundTerm> T, int freshVar) {
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
