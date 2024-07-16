package at.jku.risc.uarau;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Algorithm {
    // ~~ ~~ ~~ ~~ API ~~ ~~ ~~ ~~
    
    public static void solve(Term lhs, Term rhs, ProximityMap R, float lambda) {
        solve(lhs, rhs, R, Math::min, lambda);
    }
    
    public static void solve(Term lhs, Term rhs, ProximityMap R, TNorm tNorm, float lambda) {
        new Algorithm(R, tNorm, lambda).run(lhs, rhs);
    }
    
    // ~~ ~~ ~~ ~~ IMPLEMENTATION ~~ ~~ ~~ ~~
    Logger log = LoggerFactory.getLogger(Algorithm.class);
    
    private final ProximityMap R;
    private final TNorm tNorm;
    private final float lambda;
    
    private Algorithm(ProximityMap r, TNorm tNorm, float lambda) {
        R = r;
        this.tNorm = tNorm;
        this.lambda = lambda;
    }
    
    private void run(Term lhs, Term rhs) {
        // TODO analyze for correspondence/mapping properties
        
        Deque<Config> branches = new ArrayDeque<>();
        Deque<Config> solved = new ArrayDeque<>();
        branches.push(new Config(lhs, rhs));
        log.info("Solving {}  ;  λ={}  ;  {}", branches.peek(), lambda, R);
        
        BRANCHING:
        while (!branches.isEmpty()) {
            Config cfg = branches.pop();
            while (!cfg.A.isEmpty()) {
                AUT aut = cfg.A.pop();
                // TRIVIAL
                if (aut.T1.isEmpty() && aut.T2.isEmpty()) {
                    log.debug("Trivial: {}", cfg);
                    cfg.r.push(new Substitution(aut.var, Term.ANON));
                    continue;
                }
                // DECOMPOSE
                Set<Config> children = decompose(aut, cfg);
                if (!children.isEmpty()) {
                    for (Config child : children) {
                        branches.push(child);
                    }
                    continue BRANCHING;
                }
                // SOLVE
                cfg.r.push(new Substitution(aut.var, Term.ANON));
            }
            assert (cfg.A.isEmpty());
            solved.push(cfg);
        }
        // TODO post-process
    }
    
    private Set<Config> decompose(AUT aut, Config cfg) {
        Set<Config> children = new HashSet<>();
        Set<Term> union = new HashSet<>(aut.T1);
        union.addAll(aut.T2);
        
        for (String h : R.commonProximates(union)) {
            float[] childAlpha1 = new float[]{cfg.alpha1}; // => pass by reference
            float[] childAlpha2 = new float[]{cfg.alpha2}; // (feel free to email me your opinions on this)
            List<Set<Term>> Q1 = map(h, aut.T1, childAlpha1);
            List<Set<Term>> Q2 = map(h, aut.T2, childAlpha2);
            
            // CHECK
            if (childAlpha1[0] < lambda || childAlpha2[0] < lambda) {
                continue;
            }
            if (Q1.stream().anyMatch(q -> !consistent(q, childAlpha1[0])) || Q2.stream()
                    .anyMatch(q -> !consistent(q, childAlpha2[0]))) {
                continue;
            }
            
            // APPLY
            Config child = cfg.copy();
            Term[] hArgs = new Term[R.arity(h)];
            for (int i = 0; i < hArgs.length; i++) {
                int yi = child.freshVar();
                hArgs[i] = new Term(yi);
                child.A.push(new AUT(yi, Q1.get(i), Q2.get(i)));
            }
            child.r.push(new Substitution(aut.var, new Term(h, hArgs)));
            child.alpha1 = childAlpha1[0];
            child.alpha2 = childAlpha2[0];
            
            children.add(child);
        }
        return children;
    }
    
    private List<Set<Term>> map(String h, Set<Term> T, float[] beta) {
        int hArity = R.arity(h);
        
        // Q[i] => set of args which h|i maps to
        List<Set<Term>> Q = new ArrayList<>(hArity);
        for (int i = 0; i < hArity; i++) {
            Q.add(new HashSet<>());
        }
        for (Term t : T) {
            ProximityRelation proxRelation = R.relation(h, t.head);
            for (int i = 0; i < hArity; i++) {
                for (int p : proxRelation.get(h, i)) {
                    assert (t.arguments != null); // TODO is there a better place to do this check?
                    Term tArg = t.arguments[p];
                    Q.get(i).add(tArg);
                }
            }
            beta[0] = tNorm.apply(beta[0], proxRelation.proximity);
        }
        return Q;
    }
    
    private boolean consistent(Set<Term> terms, float alpha) {
        Deque<State> init = new ArrayDeque<>();
        init.push(new State(terms, Term.UNUSED_VAR, alpha));
        Deque<Deque<State>> branches = new ArrayDeque<>();
        branches.push(init);
        
        BRANCHING:
        while (!branches.isEmpty()) {
            Deque<State> states = branches.pop();
            while (!states.isEmpty()) {
                State state = states.pop();
                State.Pair pair = state.pairs.pop();
                // REMOVE
                if (pair.T.size() <= 1) {
                    continue;
                }
                // REDUCE
                for (String h : R.commonProximates(pair.T)) {
                    float[] childAlpha = new float[]{state.alpha}; // => pass by reference
                    List<Set<Term>> Q = map(h, pair.T, childAlpha);
                    if (childAlpha[0] < lambda) {
                        continue;
                    }
                    State child = state.copy();
                    for (Set<Term> q : Q) {
                        child.pairs.push(new State.Pair(Term.UNUSED_VAR, q));
                    }
                    child.alpha = childAlpha[0];
                    states.push(child);
                }
                branches.push(states);
                continue BRANCHING;
            }
            return true;
        }
        return false;
    }
}
