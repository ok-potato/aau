package at.jku.risc.uarau.data;

import at.jku.risc.uarau.Util;

import java.util.ArrayDeque;
import java.util.Deque;

public class Config {
    public final Deque<AUT> A, S;
    public final Deque<Substitution> substitutions;
    
    public float alpha1, alpha2;
    
    private int freshVar;
    
    public Config(Term T1, Term T2) {
        A = new ArrayDeque<>();
        S = new ArrayDeque<>();
        substitutions = new ArrayDeque<>();
        alpha1 = 1.0f;
        alpha2 = 1.0f;
        freshVar = 0;
        A.push(new AUT(freshVar(), T1, T2));
    }
    
    private Config(Config original) {
        this(original, original.S);
    }
    
    private Config(Config original, Deque<AUT> S) {
        this.A = Util.copyAccurate(original.A);
        this.S = Util.copyAccurate(S);
        this.substitutions = Util.copyAccurate(original.substitutions);
        this.alpha1 = original.alpha1;
        this.alpha2 = original.alpha2;
        this.freshVar = original.freshVar;
    }
    
    public Config copy() {
        return new Config(this);
    }
    
    public Config update_S(Deque<AUT> S) {
        return new Config(this, S);
    }
    
    public int freshVar() {
        return freshVar++;
    }
    
    public int peekVar() {
        return freshVar + 1;
    }
    
    @Override
    public String toString() {
        String A_str = Util.joinString(A, " ", "➰");
        String S_str = Util.joinString(S, " ", "➰");
        String r = Substitution.apply(substitutions, Term.VAR_0).toString();
        return String.format("⚓ ⚫ %s ⚫ %s 🔅 %s ⚫ %s, %s", A_str, S_str, r, alpha1, alpha2);
    }
    
    @Override
    public int hashCode() {
        return A.hashCode();
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Config)) {
            return false;
        }
        Config that = (Config) object;
        return that.A.size() == this.A.size() && that.A.containsAll(this.A);
    }
}
