package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.DataUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class State {
    // mutable
    public final Deque<Expression> expressions;
    public final Deque<Substitution> s;
    
    private int freshVar;
    
    public State(Set<Term> T, int freshVar) {
        this.expressions = new ArrayDeque<>();
        this.s = new ArrayDeque<>();
        this.freshVar = freshVar;
        expressions.push(new Expression(freshVar(), T));
    }
    
    private State(State original) {
        this.expressions = DataUtils.newDeque(original.expressions);
        this.s = DataUtils.newDeque(original.s);
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
