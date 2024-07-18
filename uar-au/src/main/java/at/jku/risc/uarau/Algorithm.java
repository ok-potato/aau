package at.jku.risc.uarau;

import at.jku.risc.uarau.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class Algorithm {
    // ~~ ~~ ~~ ~~ API ~~ ~~ ~~ ~~
    
    public static void solve(String problem, String proximityRelations, float lambda) {
        List<Term> sides = Parser.parseProblem(problem);
        solve(sides.get(0), sides.get(1), Parser.parseProximityRelations(proximityRelations), lambda);
    }
    
    public static void solve(Term lhs, Term rhs, Collection<ProximityRelation> relations, float lambda) {
        solve(lhs, rhs, relations, Math::min, lambda);
    }
    
    public static void solve(Term lhs, Term rhs, Collection<ProximityRelation> relations, TNorm tNorm, float lambda) {
        new Algorithm(lhs, rhs, relations, tNorm, lambda).run();
    }
    
    // ~~ ~~ ~~ ~~ IMPLEMENTATION ~~ ~~ ~~ ~~
    Logger log = LoggerFactory.getLogger(Algorithm.class);
    
    private final ProximityMap R;
    private final Term lhs, rhs;
    private final TNorm tNorm;
    private final float lambda;
    
    private Algorithm(Term lhs, Term rhs, Collection<ProximityRelation> relations, TNorm tNorm, float lambda) {
        this.rhs = rhs;
        this.lhs = lhs;
        this.R = new ProximityMap(rhs, lhs, relations, lambda);
        this.tNorm = tNorm;
        this.lambda = lambda;
    }
    
    private void run() {
        // TODO analyze for correspondence/mapping properties
        
        Deque<Config> branches = new ArrayDeque<>();
        Deque<Config> solved = new ArrayDeque<>();
        Config initCfg = new Config(lhs, rhs);
        branches.push(initCfg);
        log.info("A1 █ {} █ λ={} █ {}", initCfg, lambda, R);
        
        BRANCHING:
        while (!branches.isEmpty()) {
            Config cfg = branches.pop();
            while (!cfg.A.isEmpty()) {
                AUT aut = cfg.A.pop();
                // TRIVIAL
                if (aut.T1.isEmpty() && aut.T2.isEmpty()) {
                    cfg.r.addLast(new Substitution(aut.var, Term.ANON));
                    log.debug("TRI => {}", cfg);
                    continue;
                }
                // DECOMPOSE
                Set<Config> children = decompose(aut, cfg);
                if (!children.isEmpty()) {
                    for (Config child : children) {
                        branches.push(child);
                    }
                    log.debug("DEC => {}", Util.join(children, "   ", "⚠️"));
                    continue BRANCHING;
                }
                // SOLVE
                cfg.r.addLast(new Substitution(aut.var, Term.ANON));
                log.debug("SOL => {}", cfg);
            }
            assert (cfg.A.isEmpty());
            solved.push(cfg);
        }
        // TODO post-process
        log.info("~~~~~~~~~~~~~~~~~~~~~~~~  done  ~~~~~~~~~~~~~~~~~~~~~~~~");
    }
    
    private Set<Config> decompose(AUT aut, Config cfg) {
        Set<Config> children = new HashSet<>();
        Set<String> heads = aut.T1.stream().map(t -> t.head).collect(Collectors.toSet());
        heads.addAll(aut.T2.stream().map(t -> t.head).collect(Collectors.toSet()));
        
        for (String h : R.commonProximates(heads)) {
            float[] childAlpha1 = new float[]{cfg.alpha1}; // => pass by reference
            float[] childAlpha2 = new float[]{cfg.alpha2}; // (feel free to email me your opinions on this)
            List<Set<Term>> Q1 = map(h, aut.T1, childAlpha1);
            List<Set<Term>> Q2 = map(h, aut.T2, childAlpha2);
            
            // CHECK DEC
            if (childAlpha1[0] < lambda || childAlpha2[0] < lambda) {
                continue;
            }
            if (Q1.stream().anyMatch(q -> !consistent(q, childAlpha1[0])) || Q2.stream()
                    .anyMatch(q -> !consistent(q, childAlpha2[0]))) {
                continue;
            }
            
            // APPLY DEC
            Config child = cfg.copy();
            Term[] hArgs = new Term[R.arity(h)];
            for (int i = 0; i < hArgs.length; i++) {
                int yi = child.freshVar();
                hArgs[i] = new Term(yi);
                child.A.push(new AUT(yi, Q1.get(i), Q2.get(i)));
            }
            child.r.addLast(new Substitution(aut.var, new Term(h, hArgs)));
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
            ProximityRelation proxRelation = R.proxRelation(h, t.head);
            for (int i = 0; i < hArity; i++) {
                for (int p : proxRelation.get(h, i)) {
                    assert (!t.isVar() && t.arguments != null);
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
                for (String h : R.commonProximates(pair.T.stream().map(t -> t.head).collect(Collectors.toSet()))) {
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
