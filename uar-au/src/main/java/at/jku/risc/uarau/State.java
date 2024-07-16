package at.jku.risc.uarau;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class State {
    public final Deque<Pair> pairs;
    public float alpha;
    
    private int freshVar;
    
    private State(int freshVar, float alpha) {
        this.pairs = new ArrayDeque<>();
        this.freshVar = freshVar;
        this.alpha = alpha;
    }
    
    public State(Set<Term> T, int freshVar, float alpha) {
        this(freshVar, alpha);
        pairs.push(new Pair(freshVar(), T));
    }
    
    private State(State original) {
        this.pairs = Util.copy(original.pairs);
        this.alpha = original.alpha;
        this.freshVar = original.freshVar;
    }
    
    public State copy() {
        return new State(this);
    }
    
    public int freshVar() {
        return freshVar++;
    }
    
    public static class Pair {
        public final int x;
        public final Set<Term> T;
        
        public Pair(int x, Set<Term> t) {
            this.x = x;
            T = t;
        }
    }
}
