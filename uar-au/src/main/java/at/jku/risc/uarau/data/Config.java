package at.jku.risc.uarau.data;

import at.jku.risc.uarau.Util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.StringJoiner;

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
        StringJoiner joinA = new StringJoiner("");
        A.forEach(aut -> joinA.add(aut.toString()));
        if (joinA.length() == 0) {
            joinA.add("..");
        }
        StringJoiner joinS = new StringJoiner("");
        S.forEach(aut -> joinS.add(aut.toString()));
        if (joinS.length() == 0) {
            joinS.add("..");
        }
        return String.format("⚙️⚫%s⚫%s⚫%s⚫%s,%s", joinA, joinS, r, alpha1, alpha2);
    }
}
