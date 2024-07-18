package at.jku.risc.uarau.data;

import at.jku.risc.uarau.Util;

import java.util.ArrayDeque;
import java.util.Deque;

public class Config {
    public final Deque<AUT> A, S;
    public final Deque<Substitution> r;
    public float alpha1, alpha2;
    
    private int freshVar;
    
    private Config() {
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
    
    private Config(Config original) {
        A = Util.copy(original.A);
        S = Util.copy(original.S);
        r = Util.copy(original.r);
        alpha1 = original.alpha1;
        alpha2 = original.alpha2;
        freshVar = original.freshVar;
    }
    
    public Config copy() {
        return new Config(this);
    }
    
    public int freshVar() {
        return freshVar++;
    }
    
    public int peekVar() {
        return freshVar + 1;
    }
    
    @Override
    public String toString() {
        String A_str = Util.join(A, "", "➰");
        String S_str = Util.join(S, "", "➰");
        String r_str = Util.join(r, ", ", "..");
        return String.format("⧛ ⚫%s⚫%s⚫%s⚫%s,%s ⧚", A_str, S_str, r_str, alpha1, alpha2);
    }
}
