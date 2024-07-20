package at.jku.risc.uarau.data;

import at.jku.risc.uarau.Util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class State {
    public final Deque<Expression> expressions;
    public float alpha;
    
    private int freshVar;
    
    public State(Set<Term> T, int freshVar, float alpha) {
        this.expressions = new ArrayDeque<>();
        this.freshVar = freshVar;
        this.alpha = alpha;
        expressions.push(new Expression(freshVar(), T));
    }
    
    private State(State original) {
        this.expressions = Util.copyAccurate(original.expressions);
        this.alpha = original.alpha;
        this.freshVar = original.freshVar;
    }
    
    public State copy() {
        return new State(this);
    }
    
    public int freshVar() {
        return freshVar++;
    }
    
    @Override
    public String toString() {
        return expressions.toString();
    }
}
