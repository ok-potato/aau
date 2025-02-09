package at.jku.risc.aau;

import at.jku.risc.aau.impl.Algorithm;
import at.jku.risc.aau.impl.Substitution;
import at.jku.risc.aau.term.GroundTermOrAnon;
import at.jku.risc.aau.term.GroundishTerm;
import at.jku.risc.aau.term.Term;
import at.jku.risc.aau.util.ANSI;
import at.jku.risc.aau.util.Data;
import at.jku.risc.aau.util.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The output of the {@linkplain Algorithm Algorithm} is a set of {@linkplain Solution Solutions},
 * where each solution gives a unique {@linkplain Solution#generalization} of the problem terms.
 * <br><br>
 * If enabled, each variable in the {@linkplain Solution#generalization}
 * gets an associated {@linkplain Witness#substitutions} entry per side of the equation.
 * You can apply any combination of these to get a term which approximates the respective problem term.
 * You can get all possible approximating terms with {@linkplain Solution#enumerate()}.
 * <br><br>
 * {@linkplain Solution#alpha1} and {@linkplain Solution#alpha2} are the maximum proximities you can get for each side.
 */
public class Solution {
    public final Term generalization;
    public final Witness lhs, rhs;
    public final float alpha1, alpha2;
    
    public Solution(Term generalization, Witness lhs, Witness rhs, float alpha1, float alpha2) {
        this.generalization = generalization;
        assert (lhs == null) == (rhs == null);
        if (lhs != null) {
            assert lhs.substitutions.keySet().equals(rhs.substitutions.keySet());
            this.lhs = lhs;
            this.rhs = rhs;
        } else {
            this.lhs = null;
            this.rhs = null;
        }
        this.alpha1 = alpha1;
        this.alpha2 = alpha2;
    }
    
    public Pair<Set<GroundishTerm>, Set<GroundishTerm>> enumerate() {
        return Pair.of(enumerateSide(lhs.substitutions), enumerateSide(rhs.substitutions));
    }
    
    private Set<GroundishTerm> enumerateSide(Map<Integer, Set<GroundishTerm>> substitutions) {
        List<Set<Substitution>> steps = substitutions.entrySet().stream()
                .map(entry -> entry.getValue().stream()
                        .map(term -> new Substitution(entry.getKey(), term))
                        .collect(Collectors.toSet()))
                .collect(Collectors.toList());
        
        Set<Term> generalization = Collections.singleton(this.generalization);
        Set<Term> unchaste = Data.permute(generalization, steps, ((term, substitution) -> substitution.apply(term)));
        return unchaste.stream().map(GroundTermOrAnon::force).collect(Collectors.toSet());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(generalization);
        if (lhs == null) {
            sb.append(ANSI.red("  ∅")).append(ANSI.yellow("  α..", alpha1, alpha2));
        } else {
            sb.append(ANSI.yellow("  LHS (α=" + alpha1 + ")..  "))
                    .append(lhs)
                    .append(ANSI.yellow("  RHS (α=" + alpha2 + ")..  "))
                    .append(rhs);
        }
        return sb.toString();
    }
}
