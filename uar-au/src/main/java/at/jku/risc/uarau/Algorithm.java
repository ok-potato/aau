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
        log.info("SOLVING  ::  λ={}\n                   ::  {}{}", lambda, initCfg.A.peek(), R.toString("\n                   ::  "));
        
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
                    log.debug("DEC => {}", Util.joinString(children, "   ", "⚠️"));
                    continue BRANCHING;
                }
                // SOLVE
                cfg.S.addLast(aut);
                log.debug("SOL => {}", cfg);
            }
            assert (cfg.A.isEmpty());
            solved.push(cfg);
        }
        log.info("SOLVED: {}", Util.joinString(solved, "\n                   ::          ", "--"));
        // TODO post-process
        
        log.info("~~~~~~~~~~~~~~~~~~~~~~~~  done  ~~~~~~~~~~~~~~~~~~~~~~~~");
    }
    
    private Set<Config> decompose(AUT aut, Config cfg) {
        Set<Config> children = new HashSet<>();
        Set<String> heads = aut.T1.stream().map(t -> t.head).collect(Collectors.toSet());
        heads.addAll(aut.T2.stream().map(t -> t.head).collect(Collectors.toSet()));
        
        for (String h : R.commonProximates(heads)) {
            float[] mapAlpha1 = new float[]{cfg.alpha1}; // => pass by reference
            float[] mapAlpha2 = new float[]{cfg.alpha2}; // (feel free to email me your opinions on this)
            List<Set<Term>> Q1 = map(h, aut.T1, mapAlpha1);
            List<Set<Term>> Q2 = map(h, aut.T2, mapAlpha2);
            
            // CHECK DEC
            if (mapAlpha1[0] < lambda || mapAlpha2[0] < lambda) {
                continue;
            }
            assert (Q1 != null && Q2 != null);
            if (Q1.stream().anyMatch(q -> !consistent(q, mapAlpha1[0])) || Q2.stream()
                    .anyMatch(q -> !consistent(q, mapAlpha2[0]))) {
                continue;
            }
            
            // APPLY DEC
            Config child = cfg.copy();
            Term[] hArgs = new Term[R.arity(h)];
            for (int i = 0; i < hArgs.length; i++) {
                int yi = child.freshVar();
                hArgs[i] = new Term(yi);
                child.A.addLast(new AUT(yi, Q1.get(i), Q2.get(i)));
            }
            child.r.addLast(new Substitution(aut.var, new Term(h, hArgs)));
            child.alpha1 = mapAlpha1[0];
            child.alpha2 = mapAlpha2[0];
            
            children.add(child);
        }
        return children;
    }
    
    private List<Set<Term>> map(String h, Set<Term> T, float[] beta) {
        int h_arity = R.arity(h);
        List<Set<Term>> Q = new ArrayList<>(h_arity);
        for (int i = 0; i < h_arity; i++) {
            Q.add(new HashSet<>());
        }
        for (Term t : T) {
            assert (!t.isVar() && t.arguments != null);
            ProximityRelation proximityRelation = R.proximityRelation(h, t.head);
            List<List<Integer>> h_to_t = proximityRelation.argRelation;
            for (int i = 0; i < h_arity; i++) {
                for (int t_mapped_idx : h_to_t.get(i)) {
                    Term tArg = t.arguments[t_mapped_idx];
                    // Q[i] => set of args which h|i maps to
                    Q.get(i).add(tArg);
                }
            }
            beta[0] = tNorm.apply(beta[0], proximityRelation.proximity);
            if (beta[0] < lambda) {
                return null; // if this gets dereferenced, there's a bug somewhere else
            }
        }
        return Q;
    }
    
    private boolean consistent(Set<Term> terms, float alpha) {
        assert (!(alpha < lambda));
        Deque<State> branches = new ArrayDeque<>();
        branches.push(new State(terms, Term.UNUSED_VAR, alpha));
        log.trace("  {}", branches);
        
        BRANCHING:
        while (!branches.isEmpty()) {
            State state = branches.pop();
            while (!state.expressions.isEmpty()) {
                Expression expr = state.expressions.pop();
                // REMOVE
                if (expr.T.size() <= 1) {
                    continue;
                }
                // REDUCE
                for (String h : R.commonProximates(expr.T.stream().map(t -> t.head).collect(Collectors.toSet()))) {
                    float[] mapAlpha = new float[]{state.alpha}; // => pass by reference
                    List<Set<Term>> Q = map(h, expr.T, mapAlpha);
                    if (mapAlpha[0] < lambda) {
                        continue;
                    }
                    assert (Q != null);
                    State childState = state.copy();
                    for (Set<Term> q : Q) {
                        childState.expressions.push(new Expression(Term.UNUSED_VAR, q));
                    }
                    childState.alpha = mapAlpha[0];
                    branches.push(childState);
                    log.trace("  RED => {}", childState);
                }
                continue BRANCHING;
            }
            log.trace("  => consistent");
            return true;
        }
        log.trace("  => NOT consistent");
        return false;
    }
}
