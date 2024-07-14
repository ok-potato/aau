package at.jku.risc.uarau;

import java.util.*;

public class Algorithm {
    public static void solve(Term lhs, Term rhs, ProximityMap R, float lambda) {
        solve(lhs, rhs, R, Math::min, lambda);
    }
    
    public static void solve(Term lhs, Term rhs, ProximityMap R, TNorm tNorm, float lambda) {
        // TODO analyze for correspondence/mapping properties
        
        Deque<Config> branches = new ArrayDeque<>();
        Deque<Config> solved = new ArrayDeque<>();
        branches.add(new Config(lhs, rhs));
        
        branching:
        while (!branches.isEmpty()) {
            Config cfg = branches.pop();
            while (!cfg.A.isEmpty()) {
                AUT aut = cfg.A.pop();
                // CHECK TRIVIAL
                if (aut.T1.isEmpty() && aut.T2.isEmpty()) {
                    // APPLY TRIVIAL
                    cfg.r.push(new Substitution(aut.var, Term.ANON));
                    continue;
                }
                // CHECK DECOMPOSE
                Set<Term> union = new HashSet<>(aut.T1);
                union.addAll(aut.T2);
                boolean dec = false;
                for (String h : R.commonProximates(union)) {
                    float[] beta1 = new float[]{1.0f}; // -> pass by reference (feel free to email me your opinions on this)
                    List<Set<Term>> Q1 = map(h, aut.T1, R, beta1, tNorm);
                    if (Q1.stream().anyMatch(q -> !consistent(q))) {
                        continue;
                    }
                    float[] beta2 = new float[]{1.0f}; // -> pass by reference
                    List<Set<Term>> Q2 = map(h, aut.T2, R, beta2, tNorm);
                    if (Q2.stream().anyMatch(q -> !consistent(q))) {
                        continue;
                    }
                    // APPLY DECOMPOSE
                    dec = true;
                    Config child = cfg.copy();
                    Term[] hArgs = new Term[R.arity(h)];
                    for (int i = 0; i < hArgs.length; i++) {
                        int yi = child.freshVar();
                        hArgs[i] = new Term(yi);
                        child.A.add(new AUT(yi, Q1.get(i), Q2.get(i)));
                    }
                    child.r.add(new Substitution(aut.var, new Term(h, hArgs)));
                    child.alpha1 = tNorm.apply(child.alpha1, beta1[0]);
                    child.alpha2 = tNorm.apply(child.alpha2, beta2[0]);
                    branches.push(child);
                }
                // CHECK SOLVE
                if (dec) {
                    continue branching;
                }
                // APPLY SOLVE
                cfg.r.add(new Substitution(aut.var, Term.ANON));
            }
            solved.add(cfg);
        }
        // TODO post-process
    }
    
    private static List<Set<Term>> map(String h, Set<Term> T, ProximityMap R, float[] beta, TNorm tNorm) {
        int hArity = R.arity(h);
        List<Set<Term>> Q = new ArrayList<>(hArity);
        for (int i = 0; i < hArity; i++) {
            Q.add(new HashSet<>());
        }
        
        for (Term t : T) {
            ProximityRelation proxRelation = R.relation(h, t.head);
            beta[0] = tNorm.apply(beta[0], proxRelation.proximity);
            for (int i = 0; i < hArity; i++) {
                for (int p : proxRelation.get(h, i)) {
                    Term tArg = t.arguments[p];
                    Q.get(i).add(tArg);
                }
            }
        }
        return Q;
    }
    
    private static boolean consistent(Set<Term> T) {
        // TODO
    }
}
