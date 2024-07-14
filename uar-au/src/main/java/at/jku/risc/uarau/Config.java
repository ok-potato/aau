package at.jku.risc.uarau;

import java.util.ArrayDeque;
import java.util.Deque;

public class Config {
    public final Deque<AUT> A, S;
    public final Deque<Substitution> r;
    public float alpha1, alpha2;
    
    private int freshVar;
    
    public Config() {
        A = new ArrayDeque<>();
        S = new ArrayDeque<>();
        r = new ArrayDeque<>();
        alpha1 = 1.0f;
        alpha2 = 1.0f;
        freshVar = 0;
    }
    
    public Config(Term T1, Term T2) {
        this();
        A.push(new AUT(freshVar(), T1, T2));
    }
    
    private Config(Config cfg) {
        A = new ArrayDeque<>(cfg.A);
        S = new ArrayDeque<>(cfg.S);
        r = new ArrayDeque<>(cfg.r);
        alpha1 = cfg.alpha1;
        alpha2 = cfg.alpha2;
        freshVar = cfg.freshVar;
    }
    
    public Config copy() {
        return new Config(this);
    }
    
    public int freshVar() {
        return freshVar++;
    }
}
