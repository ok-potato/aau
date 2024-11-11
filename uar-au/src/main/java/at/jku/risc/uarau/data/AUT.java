package at.jku.risc.uarau.data;

import at.jku.risc.uarau.data.term.GroundTerm;
import at.jku.risc.uarau.data.term.Term;
import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Pair;
import at.jku.risc.uarau.util.Util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * {@linkplain AUT Anti-Unification Triples} are part of a {@linkplain Config Config's} description of a program state.
 * <br><br>
 * {@linkplain AUT#T1} and {@linkplain AUT#T2} are possible LHS and RHS substitutions for the {@linkplain AUT#variable}.
 * <br>
 * The triples that appear in a final {@linkplain Config#S} are used to generate that solution's {@linkplain Witness} substitutions.
 */
public class AUT {
    public final int variable;
    public final ArraySet<GroundTerm> T1, T2;
    
    private Integer hash = null;
    
    public AUT(int variable, ArraySet<GroundTerm> T1, ArraySet<GroundTerm> T2) {
        this.variable = variable;
        this.T1 = T1;
        this.T2 = T2;
    }
    
    // TODO documentation
    public static Pair<Set<Term>, Set<Term>> applyAll(Queue<AUT> auts, Term baseTerm) {
        Pair<Set<Term>, Set<Term>> applied = new Pair<>(new HashSet<>(), new HashSet<>());
        applied.left.add(baseTerm);
        applied.right.add(baseTerm);
        
        for (AUT aut : auts) {
            applied = aut.apply(applied.left, applied.right);
        }
        return applied;
    }
    
    public Pair<Set<Term>, Set<Term>> apply(Set<Term> Q1, Set<Term> Q2) {
        Set<Term> lhs = new HashSet<>();
        Set<Term> rhs = new HashSet<>();
        
        Q1.forEach(q1 -> T1.forEach(t1 -> lhs.add(new Substitution(variable, t1).apply(q1))));
        Q2.forEach(q2 -> T2.forEach(t2 -> rhs.add(new Substitution(variable, t2).apply(q2))));
        
        return new Pair<>(Collections.unmodifiableSet(lhs), Collections.unmodifiableSet(rhs));
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s]==[%s]", ANSI.blue(variable), Util.str(T1), Util.str(T2));
    }
    
    @Override
    public int hashCode() {
        if (hash == null) {
            hash = variable + T1.hashCode() * 31 + T2.hashCode() * 31 * 31;
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AUT)) {
            return false;
        }
        AUT aut = (AUT) obj;
        if (hashCode() != aut.hashCode()) {
            return false;
        }
        return variable == aut.variable && T1.equals(aut.T1) && T2.equals(aut.T2);
    }
}
