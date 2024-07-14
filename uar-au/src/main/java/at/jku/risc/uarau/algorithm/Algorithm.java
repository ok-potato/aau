package at.jku.risc.uarau.algorithm;

/*
 * [Acronyms]
 *     mcs[r]g  -  Minimal complete set of [relevant] (R,lambda)-generalizations
 *     gdub  -  Generalization degree upper bound
 *     lin  -  Linearized version
 *
 * Given:
 * R, lambda, and the ground terms t_1, ..., t_n, n>=2
 *
 * Find:
 * A set S of tuples (r, sigma_1, ..., sigma_n, alpha_1, ..., alpha_n) s.t.
 *     - {r | (r,...) in S} is an (R, lambda)-mcsrg of t_1, ..., t_n,
 *     - r[sigma_i] ~=R,lambda t_i  and  alpha_i = gdub_R,lambda(r,t_i),  1 <= i <= n,
 *       for each (r, sigma_1, ..., sigma_n, alpha_1, ..., alpha_n) in S
 *
 * Can solve the AU-problem for four versions of argument relations; each also with lin version:
 *   - unrestricted -A1> mcsrg
 *   - correspondence relation -A1> mcsg [=^= mcsrg]
 *   - mapping -A2> mcsrg
 *   - correspondence mapping -A2> mcsg [=^= mcsrg, I assume]
 */

import at.jku.risc.uarau.data.AUT;
import at.jku.risc.uarau.data.Function;
import at.jku.risc.uarau.data.Term;
import at.jku.risc.uarau.data.Variable;

import java.util.HashSet;
import java.util.Set;

public class Algorithm {
    // ..,;-:-*^'°'^*-:-;,.. API ..,;-:-*^'°'^*-:-;,..
    public static void solve(Problem p) {
        new Algorithm(p).a1();
    }
    
    private final Problem p;
    
    private Algorithm(Problem problem) {
        this.p = problem;
    }
    
    private void a1() {
        Configuration init = new Configuration();
        Configuration cfg = applyRules(init, new AUT(init.generalization, p.lhs, p.rhs));
    }
    
    private Configuration applyRules(Configuration cfg, AUT aut) {
        cfg = new Configuration(cfg);
        // Tri: Trivial
        if (trivial(cfg, aut)) {
            return cfg;
        }
        // Dec: Decomposition
        if (decomposition(cfg, aut)) {
            return cfg;
        }
        // Sol: Solving
        cfg.solved.add(aut);
        return cfg;
    }
    
    private boolean trivial(Configuration cfg, AUT aut) {
        // O(1)
        if (aut.T1.isEmpty() && aut.T2.isEmpty()) {
            cfg.substitution.put(aut.variable, Variable.ANON);
            return true;
        }
        return false;
    }
    
    private boolean decomposition(Configuration cfg, AUT aut) {
        // did trivial -> union has 1 or more elements
        Set<Term> union = new HashSet<>(aut.T1);
        union.addAll(aut.T2);
        Set<String> common = p.R.commonApproximates(union);
        // TODO possibly multiple arg maps h -> t per t
        for (String h : common) {
            // TODO arity of h
            cfg.substitution.put(aut.variable, new Function(h));
        }
        
        //        Set<String> heads = aut.T1.stream().map(t -> )
        //
        //                Set < String > h = R.relations.stream().filter(pr -> {
        //                    if (pr.f() == aut)
        //                })
        
        return true;
    }
    
    private boolean consistent(Set<Term> T) {
        // TODO
        return false;
    }
}
