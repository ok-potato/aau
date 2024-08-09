package at.jku.risc.uarau;

import at.jku.risc.uarau.data.*;
import at.jku.risc.uarau.util.DataUtils;
import at.jku.risc.uarau.util.Pair;
import at.jku.risc.uarau.util.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public final class Algorithm {
    public static Set<Config> solve(String problem, String proximityRelations, float lambda) {
        List<Term> sides = Parser.parseProblem(problem);
        return solve(sides.get(0), sides.get(1), Parser.parseProximityRelations(proximityRelations), lambda, Math::min, false, true);
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
        Config initCfg = new Config(lhs, rhs);
        log.info("SOLVING  ::  λ={}\n                   ::  {}{}", lambda, initCfg.A.peek(), R.toString("\n                   ::  "));
        
        Deque<Config> branches = new ArrayDeque<>();
        branches.push(initCfg);
        Deque<Config> linearSolutions = new ArrayDeque<>();
        
        // APPLY RULES
        BRANCHING:
        while (!branches.isEmpty()) {
            assert(branches.stream().distinct().count() == branches.size());
            Config config = branches.removeFirst();
            while (!config.A.isEmpty()) {
                AUT aut = config.A.removeFirst();
                // TRIVIAL
                if (aut.T1.isEmpty() && aut.T2.isEmpty()) {
                    config.substitutions.addLast(new Substitution(aut.var, Term.ANON));
                    log.debug("TRI => {}", config);
                    continue;
                }
                // DECOMPOSE
                Set<Config> children = decompose(aut, config);
                if (!children.isEmpty()) {
                    for (Config child : children) {
                        branches.addLast(child);
                    }
                    log.debug("DEC => {}", DataUtils.joinString(children, " ", ""));
                    continue BRANCHING;
                }
                // SOLVE
                config.S.addLast(aut);
                log.debug("SOL => {}", config);
            }
            assert (config.A.isEmpty());
            linearSolutions.addLast(config);
        }
        log.debug("Common proximate memory ({}): {}", R.proximatesMemory.size(), R.proximatesMemory);
        
        // POST PROCESS
        assert (linearSolutions.stream().distinct().count() == linearSolutions.size());
        log.info("Solutions (LINEAR):\n                   ::  {}", DataUtils.joinString(linearSolutions, "\n                   ::  ", "--"));
        if (linear && !witness) {
            return new HashSet<>(linearSolutions);
        }
        // EXPAND
        Deque<Config> expandedSolutions = new ArrayDeque<>(linearSolutions.size());
        for (Config linearSolution : linearSolutions) {
            Deque<AUT> S_expanded = new ArrayDeque<>();
            for (AUT aut : linearSolution.S) {
                Pair<Set<Term>, Set<Term>> C = pairConjunction(aut.T1, aut.T2, linearSolution.peekVar());
                S_expanded.addLast(new AUT(aut.var, C.a, C.b));
            }
            expandedSolutions.addLast(linearSolution.update_S(S_expanded));
        }
        
        assert (expandedSolutions.stream().distinct().count() == expandedSolutions.size());
        log.info("Solutions (EXPANDED):\n                   ::  {}", DataUtils.joinString(expandedSolutions, "\n                   ::  ", "--"));
        if (linear) {
            listWitnesses(expandedSolutions);
            return new HashSet<>(expandedSolutions);
        }
        
        // MERGE
        Deque<Config> mergedSolutions = new ArrayDeque<>();
        for (Config expandedSolution : expandedSolutions) {
            Deque<AUT> S_expanded = DataUtils.copyDeque(expandedSolution.S);
            Deque<AUT> S_merged = new ArrayDeque<>();
            while (!S_expanded.isEmpty()) {
                int freshVar = expandedSolution.peekVar();
                
                AUT collector = S_expanded.removeFirst();
                Set<Term> R11 = new HashSet<>(collector.T1);
                Set<Term> R12 = new HashSet<>(collector.T2);
                
                Deque<AUT> S_expanded_rest = new ArrayDeque<>();
                Deque<Integer> X = new ArrayDeque<>();
                X.add(collector.var);
                for (AUT merging : S_expanded) {
                    // CHECK MERGE
                    Triple<Set<Term>, Set<Term>, Integer> Q = merge(R11, merging.T1, R12, merging.T2, freshVar);
                    
                    if (Q.a.isEmpty() || Q.b.isEmpty()) {
                        // couldn't merge
                        S_expanded_rest.add(merging);
                        continue;
                    }
                    // APPLY MERGE
                    X.add(merging.var);
                    R11 = Q.a;
                    R12 = Q.b;
                    freshVar = Q.c;
                }
                S_expanded = S_expanded_rest;
                if (X.size() == 1) { // nothing merged
                    S_merged.addLast(collector);
                } else {
                    Term y = new Term(freshVar);
                    for (int x_i : X) {
                        expandedSolution.substitutions.addLast(new Substitution(x_i, y));
                    }
                    S_merged.addLast(new AUT(freshVar, R11, R12));
                }
            }
            mergedSolutions.addLast(expandedSolution.update_S(S_merged));
        }
        assert (mergedSolutions.stream().allMatch(merged -> isLinear(merged.S)));
        log.info("Solutions (MERGED):\n                   ::  {}", DataUtils.joinString(mergedSolutions, "\n                   ::  ", "--"));
        listWitnesses(mergedSolutions);
        log.info("~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~");
        return new HashSet<>(mergedSolutions);
    }
    
    private boolean isLinear(Collection<AUT> S) {
        Set<Integer> occurred = new HashSet<>();
        for (AUT aut : S) {
            if (occurred.contains(aut.var)) {
                return false;
            }
            occurred.add(aut.var);
        }
        return true;
    }
    
    private Set<Config> decompose(AUT aut, Config cfg) {
        Set<Config> children = new HashSet<>();
        for (String h : R.commonProximates(aut.heads())) {
            Pair<List<Set<Term>>, Float> map1 = map(h, aut.T1, cfg.alpha1);
            List<Set<Term>> Q1 = map1.a;
            float alpha1 = map1.b;
            
            Pair<List<Set<Term>>, Float> map2 = map(h, aut.T2, cfg.alpha2);
            List<Set<Term>> Q2 = map2.a;
            float alpha2 = map2.b;
            
            // CHECK DEC
            if (alpha1 < lambda || alpha2 < lambda) {
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
                int y_i = child.freshVar();
                h_args[i] = new Term(y_i);
                child.A.addLast(new AUT(y_i, Q1.get(i), Q2.get(i)));
            }
            Term h_term = R.isMappedVar(h) ? new Term(h) : new Term(h, h_args);
            assert (!(h_term.mappedVar && h_args.length > 0));
            child.substitutions.addLast(new Substitution(aut.var, h_term));
            child.alpha1 = alpha1;
            child.alpha2 = alpha2;
            
            children.add(child);
        }
        return children;
    }
    
    private Pair<List<Set<Term>>, Float> map(String h, Set<Term> T, float beta) {
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
                    Q.get(i).add(t.arguments.get(t_mapped_idx));
                }
            }
            beta = t_norm.apply(beta, proximityRelation.proximity);
            if (beta < lambda) {
                return new Pair<>(null, beta); // if this gets dereferenced, there's a bug somewhere else
            }
        }
        return new Pair<>(Q, beta);
    }
    
    private boolean consistent(Set<Term> terms) {
        return !conjunction(terms, Term.UNUSED_VAR).a.isEmpty();
    }
    
    private Triple<Set<Term>, Set<Term>, Integer> merge(Set<Term> T11, Set<Term> T12, Set<Term> T21, Set<Term> T22, int freshVar) {
        Set<Term> T1 = new HashSet<>(T11);
        T1.addAll(T12);
        Pair<Set<Term>, Integer> C1 = conjunction(T1, freshVar);
        if (C1.a.isEmpty()) {
            return new Triple<>(C1.a, C1.a, C1.b);
        }
        
        Set<Term> T2 = new HashSet<>(T21);
        T2.addAll(T22);
        Pair<Set<Term>, Integer> C2 = conjunction(T2, C1.b);
        return new Triple<>(C1.a, C2.a, C2.b);
    }
    
    private Pair<Set<Term>, Set<Term>> pairConjunction(Set<Term> T1, Set<Term> T2, int freshVar) {
        Pair<Set<Term>, Integer> C1 = conjunction(T1, freshVar);
        freshVar = C1.b;
        Pair<Set<Term>, Integer> C2 = conjunction(T2, freshVar);
        assert (!C1.a.isEmpty() || C2.a.isEmpty());
        return new Pair<>(C1.a, C2.a);
    }
    
    private Pair<Set<Term>, Integer> conjunction(Set<Term> terms, int freshVar) {
        boolean consistencyCheck = freshVar == Term.UNUSED_VAR;
        Deque<State> branches = new ArrayDeque<>();
        terms = terms.stream().filter(t -> !Term.ANON.equals(t)).collect(Collectors.toSet());
        branches.push(new State(terms, freshVar));
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
                    state.s.addLast(new Substitution(expr.var, Term.ANON));
                    continue;
                }
                // REDUCE
                for (String h : R.commonProximates(expr.T.stream().map(t -> t.head).collect(Collectors.toSet()))) {
                    List<Set<Term>> Q = map(h, expr.T, 1.0f).a;
                    assert (Q != null);
                    State childState = state.copy();
                    
                    Term[] h_args = new Term[R.arity(h)];
                    for (int i = 0; i < h_args.length; i++) {
                        int y_i = childState.freshVar();
                        h_args[i] = new Term(y_i);
                        Q.get(i).removeIf(Term.ANON::equals);
                        childState.expressions.push(new Expression(y_i, Q.get(i)));
                    }
                    freshVar = Math.max(freshVar, childState.peekVar());
                    Term h_term = R.isMappedVar(h) ? new Term(h) : new Term(h, h_args);
                    assert (!(h_term.mappedVar && h_args.length > 0));
                    childState.s.addLast(new Substitution(expr.var, h_term));
                    branches.push(childState);
                    if (log.isTraceEnabled()) {
                        log.trace("  RED => {}", childState);
                    }
                }
                continue BRANCHING;
            }
            if (consistencyCheck) {
                log.trace("  => consistent");
                return new Pair<>(Collections.singleton(Term.ANON), freshVar);
            }
            solutions.add(Substitution.applyAll(state.s, state.peekVar()));
        }
        if (consistencyCheck) {
            log.trace("  => NOT consistent");
        } else {
            if (solutions.size() < 20) {
                log.debug("terms: {} -> conjunctions: ({}) {}", terms, solutions.size(), solutions);
            } else {
                log.debug("terms: {} -> conjunctions: ({})", terms, solutions.size());
            }
            if (log.isTraceEnabled()) {
                log.trace("  => {}", solutions);
            }
        }
        return new Pair<>(solutions, freshVar);
    }
    
    public void listWitnesses(Deque<Config> solutions) {
        for (Config solution : solutions) {
            Deque<Term> W1 = new ArrayDeque<>();
            Deque<Term> W2 = new ArrayDeque<>();
            W1.addLast(Substitution.applyAll(solution.substitutions, Term.VAR_0));
            W2.addLast(Substitution.applyAll(solution.substitutions, Term.VAR_0));
            
            log.debug("👀{}", solution.S);
            for (AUT aut : solution.S) {
                Pair<Deque<Term>, Deque<Term>> applied = aut.pairApply(W1, W2);
                W1 = applied.a;
                W2 = applied.b;
            }
            log.debug("LHS: " + DataUtils.joinString(W1, " , ", ""));
            log.debug("RHS: " + DataUtils.joinString(W2, " , ", ""));
        }
    }
}
