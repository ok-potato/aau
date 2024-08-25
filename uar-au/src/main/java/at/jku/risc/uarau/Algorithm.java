package at.jku.risc.uarau;

import at.jku.risc.uarau.data.*;
import at.jku.risc.uarau.util.ImplicitSet;
import at.jku.risc.uarau.util.Pair;
import at.jku.risc.uarau.util._Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public final class Algorithm {
    public static Set<Solution> solve(String problem, String proximityRelations, float lambda) {
        return solve(Parser.parseProblem(problem), Parser.parseProximityRelations(proximityRelations), lambda, Math::min, false, true);
    }
    
    public static Set<Solution> solve(Pair<Term, Term> problem, Collection<ProximityRelation> relations, float lambda, TNorm t_norm, boolean linear, boolean witness) {
        return new Algorithm(problem, relations, t_norm, lambda, linear, witness).run();
    }
    
    Logger log = LoggerFactory.getLogger(Algorithm.class);
    
    private final Term lhs, rhs;
    private final ProximityMap R;
    private final TNorm t_norm;
    private final float lambda;
    private final boolean linear, witness;
    
    private Algorithm(Pair<Term, Term> problem, Collection<ProximityRelation> relations, TNorm t_norm, float lambda, boolean linear, boolean witness) {
        this.lhs = problem.a;
        this.rhs = problem.b;
        this.R = new ProximityMap(lhs, rhs, relations, lambda);
        this.t_norm = t_norm;
        this.lambda = lambda;
        this.linear = linear;
        this.witness = witness;
    }
    
    private Set<Solution> run() {
        Config initCfg = new Config(lhs, rhs);
        log.info("SOLVING  🧇  {} ?= {}  🧇  λ={}{}", lhs, rhs, lambda, R.toString("\n                   ::  "));
        
        if (R.restriction == R.theoreticalRestriction) {
            log.info("The problem is of type {}.", R.restriction);
        } else {
            log.info("The problem is theoretically of type {} - but excluding relations below the λ-cut, it is of type {}.", R.theoreticalRestriction, R.restriction);
        }
        if (R.restriction.correspondence) {
            log.info("Therefore, there are no irrelevant positions, and we get the minimal complete set of generalizations.");
        }
        
        Queue<Config> branches = new ArrayDeque<>();
        branches.add(initCfg);
        Queue<Config> linearSolutions = new ArrayDeque<>();
        
        // APPLY RULES
        BRANCHING:
        while (!branches.isEmpty()) {
            assert _Data.unique(branches);
            Config config = branches.remove();
            while (!config.A.isEmpty()) {
                AUT aut = config.A.remove();
                // TRIVIAL
                if (aut.T1.isEmpty() && aut.T2.isEmpty()) {
                    config.substitutions.add(new Substitution(aut.var, Term.ANON));
                    log.debug("TRI => {}", config);
                    continue;
                }
                // DECOMPOSE
                Queue<Config> children = decompose(aut, config);
                if (!children.isEmpty()) {
                    branches.addAll(children);
                    log.debug("DEC => {}", _Data.str(children, " ", ""));
                    continue BRANCHING;
                }
                // SOLVE
                config.S.add(aut);
                log.debug("SOL => {}", config);
            }
            assert config.A.isEmpty();
            linearSolutions.add(config);
        }
        log.debug("Common proximate memory ({}): {}", R.proximatesMemory.size(), R.proximatesMemory);
        
        // POST PROCESS
        assert _Data.unique(linearSolutions);
        log.info("Solutions (LINEAR):\n                   ::  {}", _Data.str(linearSolutions, "\n                   ::  ", "--"));
        if (linear && !witness) {
            Set<Solution> solutions = linearSolutions.stream().map(this::toSolution).collect(Collectors.toSet());
            log.info("██");
            return solutions;
        }
        // EXPAND
        Queue<Config> expandedSolutions = new ArrayDeque<>(linearSolutions.size());
        for (Config linearSolution : linearSolutions) {
            Queue<AUT> S_expanded = linearSolution.S.stream()
                    .map(aut -> expand(aut, linearSolution.peekVar()))
                    .collect(_Data.toQueue());
            expandedSolutions.add(linearSolution.copy_update_S(S_expanded));
        }
        
        assert _Data.unique(expandedSolutions);
        if (expandedSolutions.size() == linearSolutions.size() && expandedSolutions.containsAll(linearSolutions)) {
            log.info("Solutions (EXPANDED): 〃");
        } else {
            log.info("Solutions (EXPANDED):\n                   ::  {}", _Data.str(expandedSolutions, "\n                   ::  ", "--"));
        }
        if (linear) {
            Set<Solution> solutions = expandedSolutions.stream().map(this::toSolution).collect(Collectors.toSet());
            log.info("██");
            return solutions;
        }
        
        // MERGE
        Queue<Config> mergedSolutions = new ArrayDeque<>();
        for (Config expandedSolution : expandedSolutions) {
            Queue<AUT> S_expanded = new ArrayDeque<>(expandedSolution.S);
            Queue<AUT> S_merged = new ArrayDeque<>();
            while (!S_expanded.isEmpty()) {
                int freshVar = expandedSolution.peekVar();
                
                AUT merger = S_expanded.remove();
                Queue<Term> R11 = new ArrayDeque<>(merger.T1);
                Queue<Term> R12 = new ArrayDeque<>(merger.T2);
                
                Queue<AUT> unmerged = new ArrayDeque<>();
                Queue<Integer> mergedVars = new ArrayDeque<>();
                mergedVars.add(merger.var);
                for (AUT candidate : S_expanded) {
                    AUT merged = merge(R11, candidate.T1, R12, candidate.T2, freshVar);
                    if (merged.T1.isEmpty() || merged.T2.isEmpty()) {
                        unmerged.add(candidate);
                    } else { // APPLY MERGE
                        mergedVars.add(candidate.var);
                        R11 = merged.T1;
                        R12 = merged.T2;
                        freshVar = merged.var;
                    }
                }
                S_expanded = unmerged;
                if (mergedVars.size() == 1) { // nothing merged -> substitution would be redundant
                    S_merged.add(merger);
                } else {
                    final Term y = new Term(freshVar);
                    mergedVars.forEach(var -> expandedSolution.substitutions.add(new Substitution(var, y)));
                    S_merged.add(new AUT(freshVar, R11, R12));
                }
            }
            Config mergedSolution = expandedSolution.copy_update_S(S_merged);
            assert _Data.unique(mergedSolution.S);
            mergedSolutions.add(mergedSolution);
        }
        if (mergedSolutions.size() == expandedSolutions.size() && mergedSolutions.containsAll(expandedSolutions)) {
            log.info("Solutions (MERGED): 〃");
        } else {
            log.info("Solutions (MERGED):\n                   ::  {}", _Data.str(mergedSolutions, "\n                   ::  ", "--"));
        }
        Set<Solution> solutions = mergedSolutions.stream().map(this::toSolution).collect(Collectors.toSet());
        log.info("██");
        return solutions;
    }
    
    private Queue<Config> decompose(AUT aut, Config cfg) {
        Queue<Config> children = new ArrayDeque<>();
        for (String h : R.commonProximates(_Data.merge(aut.T1, aut.T2))) {
            Pair<List<Queue<Term>>, Float> map1 = map(h, aut.T1, cfg.alpha1);
            List<Queue<Term>> Q1 = map1.a;
            float alpha1 = map1.b;
            
            Pair<List<Queue<Term>>, Float> map2 = map(h, aut.T2, cfg.alpha2);
            List<Queue<Term>> Q2 = map2.a;
            float alpha2 = map2.b;
            
            // CHECK DEC
            if (alpha1 < lambda || alpha2 < lambda) {
                continue;
            }
            assert Q1 != null && Q2 != null;
            if (!R.restriction.mapping) {
                if (Q1.stream().anyMatch(q -> !consistent(q)) || Q2.stream().anyMatch(q -> !consistent(q))) {
                    continue;
                }
            }
            
            // APPLY DEC
            Config child = cfg.copy();
            Term[] h_args = new Term[R.arity(h)];
            for (int i = 0; i < h_args.length; i++) {
                int y_i = child.freshVar();
                h_args[i] = new Term(y_i);
                child.A.add(new AUT(y_i, Q1.get(i), Q2.get(i)));
            }
            Term h_term = R.isMappedVar(h) ? new Term(h) : new Term(h, h_args);
            child.substitutions.add(new Substitution(aut.var, h_term));
            child.alpha1 = alpha1;
            child.alpha2 = alpha2;
            
            children.add(child);
        }
        return children;
    }
    
    private Pair<List<Queue<Term>>, Float> map(String h, Queue<Term> T, float beta) {
        int h_arity = R.arity(h);
        List<Set<Term>> Q_sets = new ArrayList<>(h_arity);
        for (int i = 0; i < h_arity; i++) {
            Q_sets.add(new HashSet<>());
        }
        for (Term t : T) {
            assert !t.isVar() && t.arguments != null;
            ProximityRelation proximityRelation = R.proximityRelation(h, t.head);
            List<List<Integer>> h_to_t = proximityRelation.argRelation;
            for (int i = 0; i < h_arity; i++) {
                for (int t_mapped_idx : h_to_t.get(i)) {
                    // Q[i] => set of args which h|i maps to
                    Q_sets.get(i).add(t.arguments.get(t_mapped_idx));
                }
            }
            beta = t_norm.apply(beta, proximityRelation.proximity);
            if (beta < lambda) {
                return new Pair<>(null, beta); // should not be dereferenced
            }
        }
        List<Queue<Term>> Q = Q_sets.stream().map(ImplicitSet::new).collect(Collectors.toList());
        return new Pair<>(Q, beta);
    }
    
    private boolean consistent(Queue<Term> terms) {
        return specialConjunction(terms, Term.UNUSED_VAR).a != null;
    }
    
    private AUT expand(AUT aut, int freshVar) {
        Pair<Queue<Term>, Integer> pair1 = specialConjunction(aut.T1, freshVar);
        Queue<Term> C1 = pair1.a;
        freshVar = pair1.b;
        
        Pair<Queue<Term>, Integer> pair2 = specialConjunction(aut.T2, freshVar);
        Queue<Term> C2 = pair2.a;
        
        assert !C1.isEmpty() && !C2.isEmpty();
        return new AUT(aut.var, C1, C2);
    }
    
    private AUT merge(Queue<Term> T11, Queue<Term> T12, Queue<Term> T21, Queue<Term> T22, int freshVar) {
        Pair<Queue<Term>, Integer> pair1 = specialConjunction(_Data.merge(T11, T12), freshVar);
        Queue<Term> Q1 = pair1.a;
        freshVar = pair1.b;
        
        if (Q1.isEmpty()) { // optimization: merge fails if either side is empty, so we can stop here
            return new AUT(freshVar, Q1, Q1);
        }
        
        Pair<Queue<Term>, Integer> pair2 = specialConjunction(_Data.merge(T21, T22), freshVar);
        Queue<Term> Q2 = pair2.a;
        freshVar = pair2.b;
        
        return new AUT(freshVar, Q1, Q2);
    }
    
    private final Pair<Queue<Term>, Integer> consistent = new Pair<>(new ImplicitSet<>(), Term.UNUSED_VAR);
    
    private Pair<Queue<Term>, Integer> specialConjunction(Queue<Term> terms, int freshVar) {
        boolean consistencyCheck = freshVar == Term.UNUSED_VAR;
        Queue<State> branches = new ArrayDeque<>();
        assert !terms.contains(Term.ANON);
        terms = terms.stream().filter(t -> !Term.ANON.equals(t)).collect(_Data.toQueue());
        branches.add(new State(terms, freshVar));
        
        if (consistencyCheck) {
            log.trace("  cons: {}", _Data.str(branches.peek().expressions.peek().T));
        }
        
        Queue<Term> solutions = consistencyCheck ? null : new ArrayDeque<>();
        BRANCHING:
        while (!branches.isEmpty()) {
            State state = branches.remove();
            while (!state.expressions.isEmpty()) {
                Expression expr = state.expressions.remove();
                Queue<Term> exprT = expr.T.stream().filter(t -> !Term.ANON.equals(t)).collect(_Data.toQueue());
                // REMOVE
                if (consistencyCheck && exprT.size() <= 1 || exprT.isEmpty()) {
                    state.s.add(new Substitution(expr.var, Term.ANON));
                    continue;
                }
                // REDUCE
                for (String h : R.commonProximates(exprT)) {
                    List<Queue<Term>> Q = map(h, exprT, 1.0f).a;
                    assert Q != null;
                    State childState = state.copy();
                    
                    Term[] h_args = new Term[R.arity(h)];
                    for (int i = 0; i < h_args.length; i++) {
                        int y_i = childState.freshVar();
                        h_args[i] = new Term(y_i);
                        childState.expressions.add(new Expression(y_i, Q.get(i)));
                    }
                    freshVar = Math.max(freshVar, childState.peekVar());
                    Term h_term = R.isMappedVar(h) ? new Term(h) : new Term(h, h_args);
                    childState.s.add(new Substitution(expr.var, h_term));
                    branches.add(childState);
                    
                    if (log.isTraceEnabled()) {
                        log.trace("  RED: {} {}=> {}", expr, h, childState.expressions);
                    }
                }
                continue BRANCHING;
            }
            if (consistencyCheck) {
                log.trace("=> consistent");
                return consistent;
            }
            solutions.add(Substitution.applyAll(state.s, state.peekVar()));
        }
        
        if (consistencyCheck) {
            log.trace("=> NOT consistent");
        } else {
            if (solutions.size() < 16) {
                log.debug("=> conj: {} => {}", terms, solutions);
            } else {
                log.debug("=> conj: {} => ({})", terms, solutions.size());
            }
        }
        
        return new Pair<>(solutions, freshVar);
    }
    
    private Solution toSolution(Config config) {
        Term r = Substitution.applyAll(config.substitutions, Term.VAR_0);
        Pair<Witness, Witness> pair = witness ? calculateWitnesses(config, r) : new Pair<>(null, null);
        return new Solution(r, pair.a, pair.b, config.alpha1, config.alpha2);
    }
    
    private Pair<Witness, Witness> calculateWitnesses(Config config, Term r) {
        Map<Integer, Queue<Term>> W1 = new HashMap<>();
        Map<Integer, Queue<Term>> W2 = new HashMap<>();
        for (int var : r.V_named()) {
            Term varTerm = new Term(var);
            Pair<Queue<Term>, Queue<Term>> applied = AUT.applyAll(config.S, varTerm, varTerm);
            W1.put(var, applied.a);
            W2.put(var, applied.b);
        }
        return new Pair<>(new Witness(W1), new Witness(W2));
    }
}
