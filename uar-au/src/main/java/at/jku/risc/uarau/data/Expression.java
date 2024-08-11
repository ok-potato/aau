package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.UnmodifiableDeque;

import java.util.Deque;

public class Expression {
    public final int var;
    public final Deque<Term> T;
    
    public Expression(int var, Deque<Term> T) {
        this.var = var;
        this.T = new UnmodifiableDeque<>(T);
    }
    
    @Override
    public String toString() {
        return String.format("%s in %s", var, T);
    }
}
