package at.jku.risc.uarau.data;

import java.util.ArrayDeque;
import java.util.Queue;

public class State {
    // mutable
    public final Queue<Expression> expressions;
    public final Queue<Substitution> s;
    
    private int freshVar;
    
    public State(Queue<Term> T, int freshVar) {
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
        return freshVar == Term.UNUSED_VAR ? freshVar : freshVar++;
    }
    
    public int peekVar() {
        return freshVar;
    }
    
    @Override
    public String toString() {
        return expressions.toString();
    }
}
