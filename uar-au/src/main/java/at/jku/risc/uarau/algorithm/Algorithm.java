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
import at.jku.risc.uarau.data.Term;
import at.jku.risc.uarau.data.Variable;

import java.util.HashSet;
import java.util.Set;

public class Algorithm {
    // ..,;-:-*^'°'^*-:-;,.. API ..,;-:-*^'°'^*-:-;,..
    public static void solve(Term t1, Term t2) {
        a1(t1, t2);
    }
    
    private static class Configuration {
        // Tuple (A; S; r; alpha1; alpha2) - used to track state
        private final Set<AUT> unsolved, solved; // 'A', 'S'
        private Term generalization; // 'r'
        private float alpha1, alpha2;
        
        Configuration(Term generalization) {
            unsolved = new HashSet<>();
            solved = new HashSet<>();
            this.generalization = generalization;
            alpha1 = 1.0f;
            alpha2 = 1.0f;
        }
        
        Configuration(Configuration cfg) {
            unsolved = new HashSet<>(cfg.unsolved);
            solved = new HashSet<>(cfg.solved);
            generalization = cfg.generalization;
            alpha1 = cfg.alpha1;
            alpha2 = cfg.alpha2;
        }
    }
    
    private Algorithm() {
    }
    
    private static void a1(Term t1, Term t2) {
        Variable x = Variable.fresh();
        Configuration cfg = new Configuration(x);
        cfg = applyRules(cfg, new AUT(x, t1, t2));
    }
    
    private static Configuration applyRules(Configuration cfg, AUT aut) {
        // Tri: Trivial
        if (aut.T1.isEmpty() && aut.T2.isEmpty()) {
            // TODO substitution??
            aut.variable;
            return cfg;
        }
        // Dec: Decomposition
        // TODO
        // Sol: Solving
        cfg.solved.add(aut);
        return cfg;
    }
}
