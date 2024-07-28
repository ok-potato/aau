package at.jku.risc.uarau;

import at.jku.risc.uarau.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public final class Algorithm {
    public static Set<Config> solve(String problem, String proximityRelations, float lambda) {
        List<Term> sides = Parser.parseProblem(problem);
        return solve(sides.get(0), sides.get(1), Parser.parseProximityRelations(proximityRelations), lambda, Math::min, true, true);
    }
    
    public static Set<Config> solve(Term lhs, Term rhs, Collection<ProximityRelation> relations, float lambda, TNorm t_norm, boolean linear, boolean witness) {
        return new Algorithm(lhs, rhs, relations, t_norm, lambda, linear, witness).run();
    }
    
    Logger log = LoggerFactory.getLogger(Algorithm.class);
    
    private final ProximityMap R;
    private final Term lhs, rhs;
    private final TNorm t_norm;
    private final float lambda;
    private final boolean linear, witness;
    
    private Algorithm(Term lhs, Term rhs, Collection<ProximityRelation> relations, TNorm t_norm, float lambda, boolean linear, boolean witness) {
        this.rhs = rhs;
        this.lhs = lhs;
        this.R = new ProximityMap(rhs, lhs, relations, lambda);
        this.t_norm = t_norm;
        this.lambda = lambda;
        this.linear = linear;
        this.witness = witness;
    }
    
    private Set<Config> run() {
        // TODO analyze for correspondence/mapping properties
        
        Deque<Config> branches = new ArrayDeque<>();
        Deque<Config> solutionConfigs = new ArrayDeque<>();
        Config initCfg = new Config(lhs, rhs);
        branches.push(initCfg);
        log.info("SOLVING  ::  λ={}\n                   ::  {}{}", lambda, initCfg.A.peek(), R.toString("\n                   ::  "));
        
        BRANCHING:
        while (!branches.isEmpty()) {
            assert (new HashSet<>(branches).size() == branches.size());
            Config config = branches.pop();
            while (!config.A.isEmpty()) {
                AUT aut = config.A.pop();
                // TRIVIAL
                if (aut.T1.isEmpty() && aut.T2.isEmpty()) {
                    config.r.addLast(new Substitution(aut.var, Term.ANON));
                    log.debug("TRI => {}", config);
                    continue;
                }
                // DECOMPOSE
                Set<Config> children = decompose(aut, config);
                if (!children.isEmpty()) {
                    for (Config child : children) {
                        branches.push(child);
                    }
                    log.debug("DEC => {}", Util.joinString(children, "   ", "⚠️"));
                    continue BRANCHING;
                }
                // SOLVE
                config.S.addLast(aut);
                log.debug("SOL => {}", config);
            }
            assert (config.A.isEmpty());
            solutionConfigs.addLast(config);
        }
        log.debug("Common proximate memory ({}): {}", R.mem.size(), R.mem);
        
        // POST PROCESS

        log.info("Solutions (LINEAR): {}", Util.joinString(solutionConfigs, "\n                   ::  ", "--"));
        if (linear && !witness) {
            return new HashSet<>(solutionConfigs);
        }
        
        Set<Config> expandedSolutions = new HashSet<>(solutionConfigs.size());
        for (Config solution : solutionConfigs) {
            int[] freshVar = new int[]{solution.peekVar()};
            
            Deque<AUT> expanded = new ArrayDeque<>();
            for (AUT aut : solution.S) {
                Set<Term> T1 = conjunction(aut.T1, freshVar);
                Set<Term> T2 = conjunction(aut.T2, freshVar);
                expanded.addLast(new AUT(aut.var, T1, T2));
            }
            expandedSolutions.add(solution.transformSolution(expanded));
        }
        
        assert (expandedSolutions.size() == solutionConfigs.size());
        log.info("Solutions (EXPANDED): {}", Util.joinString(expandedSolutions, "\n                   ::  ", "--"));
        if (linear) {
            return expandedSolutions;
        }
        
        
        
        log.info("Hey now, don't do that");
        return null;
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
            if (Q1.stream().anyMatch(q -> !consistent(q)) || Q2.stream().anyMatch(q -> !consistent(q))) {
                continue;
            }
            
            // APPLY DEC
            Config child = cfg.copy();
            Term[] h_args = new Term[R.arity(h)];
            for (int i = 0; i < h_args.length; i++) {
                int yi = child.freshVar();
                h_args[i] = new Term(yi);
                child.A.addLast(new AUT(yi, Q1.get(i), Q2.get(i)));
            }
            Term h_term = R.isMappedVar(h) ? new Term(h) : new Term(h, h_args);
            assert (!(h_term.mappedVar && h_args.length > 0));
            child.r.addLast(new Substitution(aut.var, h_term));
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
                    // Q[i] => set of args which h|i maps to
                    Q.get(i).add(t.arguments[t_mapped_idx]);
                }
            }
            beta[0] = t_norm.apply(beta[0], proximityRelation.proximity);
            if (beta[0] < lambda) {
                return null; // if this gets dereferenced, there's a bug somewhere else
            }
        }
        return Q;
    }
    
    private boolean consistent(Set<Term> terms) {
        return !conjunction(terms, new int[]{Term.UNUSED_VAR}).isEmpty();
    }
    
    public static Set<Term> runConjunction(String term, String proximityRelations) {
        Algorithm algo = new Algorithm(Parser.parseTerm(term), Parser.parseTerm(term), Parser.parseProximityRelations(proximityRelations), Math::min, 0.0f, false, false);
        return algo.conjunction(Collections.singleton(Parser.parseTerm(term)), new int[]{0});
    }
    
    private Set<Term> conjunction(Set<Term> terms, int[] freshVar) {
        boolean consistencyCheck = freshVar[0] == Term.UNUSED_VAR;
        Deque<State> branches = new ArrayDeque<>();
        branches.push(new State(terms, freshVar[0]));
        log.trace("  {}", branches);
        
        Set<Term> solutions;
        if (consistencyCheck) {
            solutions = new HashSet<>(1);
        } else {
            solutions = new HashSet<>();
        }
        BRANCHING:
        while (!branches.isEmpty()) {
            State state = branches.pop();
            while (!state.expressions.isEmpty()) {
                Expression expr = state.expressions.pop();
                // REMOVE
                if (consistencyCheck && expr.T.size() <= 1 || expr.T.isEmpty()) {
                    state.s.addLast(new Substitution(expr.x, Term.ANON));
                    continue;
                }
                // REDUCE
                for (String h : R.commonProximates(expr.T.stream().map(t -> t.head).collect(Collectors.toSet()))) {
                    List<Set<Term>> Q = map(h, expr.T, new float[]{1.0f});
                    assert (Q != null);
                    State childState = state.copy();
                    
                    Term[] h_args = new Term[R.arity(h)];
                    for (int i = 0; i < h_args.length; i++) {
                        int yi = childState.freshVar();
                        h_args[i] = new Term(yi);
                        childState.expressions.push(new Expression(yi, Q.get(i)));
                    }
                    freshVar[0] = Math.max(freshVar[0], childState.freshVar());
                    Term h_term = R.isMappedVar(h) ? new Term(h) : new Term(h, h_args);
                    assert (!(h_term.mappedVar && h_args.length > 0));
                    childState.s.addLast(new Substitution(expr.x, h_term));
                    branches.push(childState);
                    if (log.isTraceEnabled()) {
                        log.trace("  RED => {}", childState);
                    }
                }
                continue BRANCHING;
            }
            if (consistencyCheck) {
                log.trace("  => consistent");
                return Collections.singleton(Term.ANON);
            }
            solutions.add(Substitution.apply(state.s));
        }
        if (consistencyCheck) {
            log.trace("  => NOT consistent");
        } else {
            if (log.isTraceEnabled()) {
                log.trace("  => {}", solutions);
            }
        }
        return solutions;
    }
}
