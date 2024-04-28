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

import at.jku.risc.uarau.data.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Algorithm {
    // ..,;-:-*^'°'^*-:-;,.. API ..,;-:-*^'°'^*-:-;,..
    public static void solve(Problem p) {
        new Algorithm(p.R, p.lambda).a1(p.lhs, p.rhs);
    }
    
    private final Map<Integer, String> names = new HashMap<>();
    private final float lambda;
    private final ProximityMap R;
    
    private Algorithm(ProximityMap R, float lambda) {
        this.lambda = lambda;
        this.R = R;
    }
    
    private void a1(Term t1, Term t2) {
        Configuration init = new Configuration();
        Configuration cfg = applyRules(init, new AUT(init.generalization, t1, t2));
    }
    
    private Configuration applyRules(Configuration cfg, AUT aut) {
        // TODO expensive
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
        // O(n)
        Set<Term> intersection = new HashSet<>(aut.T1);
        intersection.retainAll(aut.T2);
        if (intersection.isEmpty()) {
            return false;
        }
        // TODO
        
        return true;
    }
}
