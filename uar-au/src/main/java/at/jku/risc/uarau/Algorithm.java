package at.jku.risc.uarau;

import at.jku.risc.uarau.data.*;
import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.DataUtil;
import at.jku.risc.uarau.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core implementation of the algorithm.
 * <br>
 * You can run the Algorithm by defining a {@link Problem}, and calling {@link Problem#solve()} on it.
 * <br><br>
 * (or by calling {@link Algorithm#solve(String, String, float)}, or
 * <br>
 * {@link Algorithm#solve(Pair, Collection, float, TNorm, boolean, boolean)} directly)
 */
public class Algorithm {
    // *** api ***
    
    /**
     * Convenience method, typically use {@link Problem#solve()}
     */
    public static Set<Solution> solve(Pair<Term, Term> equation, Collection<ProximityRelation> relations, float lambda, TNorm tNorm, boolean merge, boolean witness) {
        return new Algorithm(equation, relations, tNorm, lambda, merge, witness).run();
    }
    
    /**
     * Convenience method, typically use {@link Problem#solve()}
     */
    public static Set<Solution> solve(String equation, String proximityRelations, float lambda) {
        return solve(Parser.parseEquation(equation), Parser.parseProximityRelations(proximityRelations), lambda, Math::min, true, true);
    }
    
    // *** /api ***
    
    private final Logger log = LoggerFactory.getLogger(Algorithm.class);
    private static final String LOG_NEWLINE = "\n                 :: ";
    
    private final Term lhs, rhs;
    private final ProximityMap R;
    private final TNorm tNorm;
    private final float lambda;
    private final boolean merge, witnesses;
    
    private Algorithm(Pair<Term, Term> equation, Collection<ProximityRelation> relations, TNorm tNorm, float lambda, boolean merge, boolean witnesses) {
        this.lhs = equation.left;
        this.rhs = equation.right;
        this.R = new ProximityMap(lhs, rhs, relations, lambda);
        this.tNorm = tNorm;
        if (lambda < 0.0f || lambda > 1.0f) {
            throw new IllegalArgumentException("Lambda must be in range [0,1]");
        }
        this.lambda = lambda;
        this.merge = merge;
        this.witnesses = witnesses;
    }
    
    private Set<Solution> run() {
        log.info(ANSI.green("SOLVING ") + lhs + ANSI.red(" ?= ") + rhs + ANSI.green(" λ = " + lambda)
                + R.toString(LOG_NEWLINE));
        
        if (R.restriction == R.theoreticalRestriction) {
            log.info("The problem is of type {}", R.restriction);
        } else {
            log.info("The problem is theoretically of type {} - but excluding relations below the λ-cut, it is of type {}", R.theoreticalRestriction, R.restriction);
        }
        if (R.restriction.correspondence) {
            log.info("Therefore, there are no irrelevant positions, and we get the minimal complete set of generalizations");
        }
        
        Queue<Config> linearSolutions = new ArrayDeque<>();
        
        // *** APPLY RULES ***
        
        Queue<Config> branches = new ArrayDeque<>();
        branches.add(new Config(lhs, rhs));
        
        BRANCHING:
        while (!branches.isEmpty()) {
            assert DataUtil.allUnique(branches);
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
                    if (log.isDebugEnabled()) {
                        log.debug("DEC => {}", DataUtil.str(children, " ", ""));
                    }
                    continue BRANCHING;
                }
                // SOLVE
                config.S.add(aut);
                log.debug("SOL => {}", config);
            }
            assert config.A.isEmpty();
            linearSolutions.add(config);
        }
        
        // *** POST PROCESS ***
        
        assert DataUtil.allUnique(linearSolutions);
        log.info(ANSI.green("LINEAR:") + "{}{}", LOG_NEWLINE, DataUtil.str(linearSolutions, LOG_NEWLINE, ""));
        
        if (!merge && !witnesses) {
            log.info(ANSI.green("SOLUTIONS:"));
            Set<Solution> solutions = linearSolutions.stream().map(this::toSolution).collect(Collectors.toSet());
            log.info("🧇");
            return solutions;
        }
        // EXPAND
        Queue<Config> expandedSolutions = new ArrayDeque<>(linearSolutions.size());
        for (Config linearSolution : linearSolutions) {
            Queue<AUT> S_expanded = linearSolution.S.stream()
                    .map(aut -> expand(aut, linearSolution.peekVar()))
                    .collect(DataUtil.toQueue());
            expandedSolutions.add(linearSolution.copy_update_S(S_expanded));
        }
        
        assert DataUtil.allUnique(expandedSolutions);
        if (expandedSolutions.size() == linearSolutions.size() && expandedSolutions.containsAll(linearSolutions)) {
            log.info(ANSI.green("EXPANDED:") + "{}-\"-", LOG_NEWLINE);
        } else {
            log.info(ANSI.green("EXPANDED:") + "{}{}", LOG_NEWLINE, DataUtil.str(expandedSolutions, LOG_NEWLINE, ""));
        }
        
        if (!merge) {
            log.info(ANSI.green("SOLUTIONS:"));
            Set<Solution> solutions = expandedSolutions.stream().map(this::toSolution).collect(Collectors.toSet());
            log.info("🧇");
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
                ArraySet<Term> R11 = new ArraySet<>(merger.T1);
                ArraySet<Term> R12 = new ArraySet<>(merger.T2);
                
                Queue<AUT> unmerged = new ArrayDeque<>();
                Queue<Integer> mergedVars = new ArrayDeque<>();
                mergedVars.add(merger.var);
                for (AUT candidate : S_expanded) {
                    AUT merged = merge(R11, candidate.T1, R12, candidate.T2, freshVar);
                    if (merged.T1.isEmpty() || merged.T2.isEmpty()) {
                        unmerged.add(candidate);
                    } else {
                        // APPLY MERGE
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
            assert DataUtil.allUnique(mergedSolution.S);
            mergedSolutions.add(mergedSolution);
        }
        
        if (mergedSolutions.size() == expandedSolutions.size() && mergedSolutions.containsAll(expandedSolutions)) {
            log.info(ANSI.green("MERGED:") + "{}-\"-", LOG_NEWLINE);
        } else {
            log.info(ANSI.green("MERGED:") + "{}{}", LOG_NEWLINE, DataUtil.str(mergedSolutions, LOG_NEWLINE, ""));
        }
        log.info(ANSI.green("SOLUTIONS:"));
        Set<Solution> solutions = mergedSolutions.stream().map(this::toSolution).collect(Collectors.toSet());
        log.info("🧇");
        return solutions;
    }
    
    private Queue<Config> decompose(AUT aut, Config cfg) {
        Queue<Config> children = new ArrayDeque<>();
        for (String h : R.commonProximates(ArraySet.merge(aut.T1, aut.T2))) {
            Pair<List<ArraySet<Term>>, Float> T1_mapped = map(h, aut.T1, cfg.alpha1);
            List<ArraySet<Term>> Q1 = T1_mapped.left;
            float T1_alpha = T1_mapped.right;
            
            Pair<List<ArraySet<Term>>, Float> T2_mapped = map(h, aut.T2, cfg.alpha2);
            List<ArraySet<Term>> Q2 = T2_mapped.left;
            float T2_alpha = T2_mapped.right;
            
            // CHECK DEC
            if (T1_alpha < lambda || T2_alpha < lambda) {
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
            child.alpha1 = T1_alpha;
            child.alpha2 = T2_alpha;
            
            children.add(child);
        }
        return children;
    }
    
    private Pair<List<ArraySet<Term>>, Float> map(String h, Queue<Term> T, float beta) {
        int h_arity = R.arity(h);
        List<Set<Term>> Q_mutable = new ArrayList<>(h_arity);
        for (int i = 0; i < h_arity; i++) {
            Q_mutable.add(new HashSet<>());
        }
        for (Term t : T) {
            assert !t.isVar() && t.arguments != null;
            ProximityRelation proximityRelation = R.proximityRelation(h, t.head);
            List<List<Integer>> h_to_t = proximityRelation.argRelation;
            for (int i = 0; i < h_arity; i++) {
                for (int t_mapped_idx : h_to_t.get(i)) {
                    // Q[i] => set of args which h|i maps to
                    Q_mutable.get(i).add(t.arguments.get(t_mapped_idx));
                }
            }
            beta = tNorm.apply(beta, proximityRelation.proximity);
            if (beta < lambda) {
                return new Pair<>(null, beta); // should not be dereferenced
            }
        }
        List<ArraySet<Term>> Q = Q_mutable.stream().map(ArraySet::new).collect(Collectors.toList());
        return new Pair<>(Q, beta);
    }
    
    private AUT expand(AUT aut, int freshVar) {
        Pair<Queue<Term>, Integer> T1_conjunction = specialConjunction(aut.T1, freshVar);
        Queue<Term> C1 = T1_conjunction.left;
        freshVar = T1_conjunction.right;
        
        Pair<Queue<Term>, Integer> pair2 = specialConjunction(aut.T2, freshVar);
        Queue<Term> C2 = pair2.left;
        
        assert !C1.isEmpty() && !C2.isEmpty();
        return new AUT(aut.var, C1, C2);
    }
    
    private AUT merge(ArraySet<Term> T11, ArraySet<Term> T12, ArraySet<Term> T21, ArraySet<Term> T22, int freshVar) {
        Pair<Queue<Term>, Integer> pair1 = specialConjunction(ArraySet.merge(T11, T12), freshVar);
        Queue<Term> Q1 = pair1.left;
        freshVar = pair1.right;
        
        if (Q1.isEmpty()) {
            return new AUT(freshVar, Q1, Q1);
        }
        
        Pair<Queue<Term>, Integer> pair2 = specialConjunction(ArraySet.merge(T21, T22), freshVar);
        Queue<Term> Q2 = pair2.left;
        freshVar = pair2.right;
        
        return new AUT(freshVar, Q1, Q2);
    }
    
    private Solution toSolution(Config config) {
        Term r = Substitution.applyAll(config.substitutions, Term.VAR_0);
        Pair<Witness, Witness> pair = witnesses ? calculateWitnesses(config, r) : new Pair<>(null, null);
        return new Solution(r, pair.left, pair.right, config.alpha1, config.alpha2);
    }
    
    private Pair<Witness, Witness> calculateWitnesses(Config config, Term r) {
        Map<Integer, Set<Term>> W1 = new HashMap<>();
        Map<Integer, Set<Term>> W2 = new HashMap<>();
        for (int var : r.V_named()) {
            Pair<Set<Term>, Set<Term>> applied = AUT.applyAll(config.S, new Term(var));
            W1.put(var, applied.left);
            W2.put(var, applied.right);
        }
        return new Pair<>(new Witness(W1), new Witness(W2));
    }
    
    // *** special conjunction ***
    
    private boolean consistent(ArraySet<Term> terms) {
        return runConjunction(terms, Term.UNUSED_VAR) != null;
    }
    
    // get rid of annoying null warnings
    private Pair<Queue<Term>, Integer> specialConjunction(ArraySet<Term> terms, int freshVar) {
        Pair<Queue<Term>, Integer> result = runConjunction(terms, freshVar);
        assert result != null;
        return result;
    }
    
    private final Pair<Queue<Term>, Integer> DUMMY_PAIR = new Pair<>(null, null);
    
    private Pair<Queue<Term>, Integer> runConjunction(ArraySet<Term> terms, int freshVar) {
        boolean consistencyCheck = freshVar == Term.UNUSED_VAR;
        Queue<State> branches = new ArrayDeque<>();
        assert !terms.contains(Term.ANON);
        terms = terms.filter(t -> !Term.ANON.equals(t));
        branches.add(new State(terms, freshVar));
        
        Queue<Term> solutions = consistencyCheck ? null : new ArrayDeque<>();
        BRANCHING:
        while (!branches.isEmpty()) {
            State state = branches.remove();
            while (!state.expressions.isEmpty()) {
                Expression expression = state.expressions.remove();
                ArraySet<Term> expr_T = expression.T.filter(t -> !Term.ANON.equals(t));
                // REMOVE
                if (consistencyCheck && expr_T.size() <= 1 || expr_T.isEmpty()) {
                    state.s.add(new Substitution(expression.var, Term.ANON));
                    continue;
                }
                // REDUCE
                for (String h : R.commonProximates(expr_T)) {
                    List<ArraySet<Term>> Q = map(h, expr_T, 1.0f).left;
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
                    childState.s.add(new Substitution(expression.var, h_term));
                    branches.add(childState);
                }
                continue BRANCHING;
            }
            if (consistencyCheck) {
                return DUMMY_PAIR;
            }
            solutions.add(Substitution.applyAll(state.s, state.peekVar()));
        }
        if (consistencyCheck) {
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("  conjunction: {} => {}", terms, solutions);
        }
        return new Pair<>(solutions, freshVar);
    }
}
