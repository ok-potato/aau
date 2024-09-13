package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.DataUtil;

import java.util.ArrayDeque;
import java.util.Queue;

public class Config {
    // mutable
    public final Queue<AUT> A, S;
    public final Queue<Substitution> substitutions;
    
    public float alpha1, alpha2;
    
    private int freshVar;
    
    public Config(Term T1, Term T2) {
        A = new ArrayDeque<>();
        S = new ArrayDeque<>();
        substitutions = new ArrayDeque<>();
        alpha1 = 1.0f;
        alpha2 = 1.0f;
        freshVar = 0;
        A.add(new AUT(freshVar(), T1, T2));
    }
    
    private Config(Config original) {
        this(original, original.S);
    }
    
    private Config(Config original, Queue<AUT> S) {
        this.A = new ArrayDeque<>(original.A);
        this.S = new ArrayDeque<>(S);
        this.substitutions = new ArrayDeque<>(original.substitutions);
        this.alpha1 = original.alpha1;
        this.alpha2 = original.alpha2;
        this.freshVar = original.freshVar;
    }
    
    public Config copy() {
        return new Config(this);
    }
    
    public Config copy_update_S(Queue<AUT> S) {
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
        String A_str = DataUtil.str(A, " ", "➰");
        String S_str = DataUtil.str(S, " ", "➰");
        String r = Substitution.applyAll(substitutions, Term.VAR_0).toString();
        return String.format("⚓ %s ⚫ %s 🔅 %s ⚫ %s, %s", A_str, S_str, r, alpha1, alpha2);
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
        return that.A.size() == A.size() && that.A.containsAll(A) && that.S.size() == S.size() && that.S.containsAll(S);
    }
}
