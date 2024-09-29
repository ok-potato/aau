package at.jku.risc.uarau;

import at.jku.risc.uarau.data.*;
import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Pair;
import at.jku.risc.uarau.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core implementation of the Algorithm described in the paper
 * <br>
 * <a href="https://doi.org/10.1007/978-3-031-10769-6_34">A Framework for Approximate Generalization in Quantitative Theories</a>
 * <br><br>
 * You can run the Algorithm by defining a {@linkplain Problem}, and calling {@linkplain Problem#solve()}
 * <br>
 * or directly calling {@linkplain Algorithm#solve(String, String, float)} ||
 * {@linkplain Algorithm#solve(Pair, Collection, float, TNorm, boolean, boolean)}
 */
public class Algorithm {
    // *** api ***
    
    /**
     * Convenience method for simple string-based inputs
     * <br>
     * For more complex queries, it's probably easiest to use {@linkplain Problem#solve()}
     */
    public static Set<Solution> solve(String equation, String proximityRelations, float lambda) {
        return solve(Parser.parseEquation(equation), Parser.parseProximityRelations(proximityRelations), lambda, Math::min, true, true);
    }
    
    /**
     * See {@linkplain Problem#solve()}
     */
    public static Set<Solution> solve(Pair<GroundTerm, GroundTerm> equation, Collection<ProximityRelation> relations, float lambda, TNorm tNorm, boolean merge, boolean witness) {
        return new Algorithm(equation, relations, tNorm, lambda, merge, witness).run();
    }
    
    // *** /api ***
    
    private final Logger log = LoggerFactory.getLogger(Algorithm.class);
    private static final String LOG_NEWLINE = "\n                 :: ";
    
    private final GroundTerm lhs, rhs;
    private final ProximityMap R;
    private final TNorm tNorm;
    private final float lambda;
    private final boolean merge, generateWitnesses;
    
    private Algorithm(Pair<GroundTerm, GroundTerm> equation, Collection<ProximityRelation> relations, TNorm tNorm, float lambda, boolean merge, boolean generateWitnesses) {
        this.lhs = equation.left;
        this.rhs = equation.right;
        this.R = new ProximityMap(lhs, rhs, relations, lambda);
        this.tNorm = tNorm;
        if (lambda < 0.0f || lambda > 1.0f) {
            throw new IllegalArgumentException("Lambda must be in range [0,1]");
        }
        this.lambda = lambda;
        this.merge = merge;
        this.generateWitnesses = generateWitnesses;
    }
    
    private Set<Solution> run() {
        log.info(ANSI.green("SOLVING :: ") + lhs + ANSI.red(" ?= ") + rhs + ANSI.green(":: λ = " + lambda));
        log.info(R.toString(LOG_NEWLINE));
        
        if (R.restriction == R.theoreticalRestriction) {
            log.info("The problem is of type {}", R.restriction);
        } else {
            log.info("The problem is theoretically of type {} - but excluding relations below the λ-cut, it is of type {}", R.theoreticalRestriction, R.restriction);
        }
        if (R.restriction.correspondence) {
            log.info("Therefore, there are no irrelevant positions, and we get the minimal complete set of generalizations");
        } else {
            log.info("Therefore, there might be irrelevant positions, and we are not guaranteed to get the minimal complete set of generalizations");
        }
        
        Queue<Config> linearSolutions = new ArrayDeque<>();
        
        // *** APPLY RULES ***
        
        Queue<Config> branches = new ArrayDeque<>();
        branches.add(new Config(lhs, rhs));
        
        BRANCHING:
        while (!branches.isEmpty()) {
            assert Util.allUnique(branches);
            Config config = branches.remove();
            while (!config.A.isEmpty()) {
                AUT aut = config.A.remove();
                // TRIVIAL
                if (aut.T1.isEmpty() && aut.T2.isEmpty()) {
                    config.substitutions.add(new Substitution(aut.var, MappedVariableTerm.ANON));
                    log.debug("TRI => {}", config);
                    continue;
                }
                // DECOMPOSE
                Queue<Config> children = decompose(aut, config);
                if (!children.isEmpty()) {
                    branches.addAll(children);
                    if (log.isDebugEnabled()) {
                        log.debug("DEC => {}", Util.str(children, " ", ""));
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
        
        assert Util.allUnique(linearSolutions);
        if (!merge && !generateWitnesses) {
            return generateSolutions(linearSolutions);
        } else {
            log.info(ANSI.green("LINEAR:") + "{}{}", LOG_NEWLINE, Util.str(linearSolutions, LOG_NEWLINE, ""));
        }
        
        // *** POST PROCESS ***
        
        // EXPAND
        Queue<Config> expandedSolutions = new ArrayDeque<>(linearSolutions.size());
        for (Config linearSolution : linearSolutions) {
            Queue<AUT> S_expanded = linearSolution.S.stream()
                    .map(aut -> expand(aut, linearSolution.peekVar()))
                    .collect(Util.toQueue());
            expandedSolutions.add(linearSolution.copy_update_S(S_expanded));
        }
        
        assert Util.allUnique(expandedSolutions);
        if (expandedSolutions.size() == linearSolutions.size() && expandedSolutions.containsAll(linearSolutions)) {
            log.info(ANSI.green("EXPANDED:") + "{}-\"-", LOG_NEWLINE);
        } else {
            log.info(ANSI.green("EXPANDED:") + "{}{}", LOG_NEWLINE, Util.str(expandedSolutions, LOG_NEWLINE, ""));
        }
        if (!merge) {
            return generateSolutions(expandedSolutions);
        }
        
        // MERGE
        Queue<Config> mergedSolutions = new ArrayDeque<>();
        for (Config expandedSolution : expandedSolutions) {
            Queue<AUT> S_expanded = new ArrayDeque<>(expandedSolution.S);
            Queue<AUT> S_merged = new ArrayDeque<>();
            while (!S_expanded.isEmpty()) {
                int freshVar = expandedSolution.peekVar();
                
                AUT merger = S_expanded.remove();
                ArraySet<GroundTerm> R11 = new ArraySet<>(merger.T1);
                ArraySet<GroundTerm> R12 = new ArraySet<>(merger.T2);
                
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
                if (mergedVars.size() == 1) { // nothing merged -> no need to substitute
                    S_merged.add(merger);
                } else {
                    final Term y = new VariableTerm(freshVar);
                    mergedVars.forEach(var -> expandedSolution.substitutions.add(new Substitution(var, y)));
                    S_merged.add(new AUT(freshVar, R11, R12));
                }
            }
            Config mergedSolution = expandedSolution.copy_update_S(S_merged);
            assert Util.allUnique(mergedSolution.S);
            mergedSolutions.add(mergedSolution);
        }
        
        if (mergedSolutions.size() == expandedSolutions.size() && mergedSolutions.containsAll(expandedSolutions)) {
            log.info(ANSI.green("MERGED:") + "{}-\"-", LOG_NEWLINE);
        } else {
            log.info(ANSI.green("MERGED:") + "{}{}", LOG_NEWLINE, Util.str(mergedSolutions, LOG_NEWLINE, ""));
        }
        return generateSolutions(mergedSolutions);
    }
    
    private Queue<Config> decompose(AUT aut, Config cfg) {
        Queue<Config> children = new ArrayDeque<>();
        for (String h : R.commonProximates(ArraySet.merged(aut.T1, aut.T2))) {
            // MAP
            Pair<List<ArraySet<GroundTerm>>, Float> T1_mapped = map(h, aut.T1, cfg.alpha1);
            List<ArraySet<GroundTerm>> Q1 = T1_mapped.left;
            float T1_alpha = T1_mapped.right;
            
            Pair<List<ArraySet<GroundTerm>>, Float> T2_mapped = map(h, aut.T2, cfg.alpha2);
            List<ArraySet<GroundTerm>> Q2 = T2_mapped.left;
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
            List<Term> h_args = Util.newList(R.arity(h), i -> {
                int y_i = child.freshVar();
                child.A.add(new AUT(y_i, Q1.get(i), Q2.get(i)));
                return new VariableTerm(y_i);
            });
            Term h_term = R.isMappedVar(h) ? new MappedVariableTerm(h) : new FunctionTerm(h, h_args);
            child.substitutions.add(new Substitution(aut.var, h_term));
            child.alpha1 = T1_alpha;
            child.alpha2 = T2_alpha;
            
            children.add(child);
        }
        return children;
    }
    
    private Pair<List<ArraySet<GroundTerm>>, Float> map(String h, Queue<GroundTerm> T, float beta) {
        int h_arity = R.arity(h);
        List<Set<GroundTerm>> Q_builder = Util.newList(h_arity, i -> new HashSet<>());
        for (GroundTerm t : T) {
            ProximityRelation proximityRelation = R.proximityRelation(h, t.head);
            for (int i = 0; i < h_arity; i++) {
                for (int t_mapped_idx : proximityRelation.argRelation.get(i)) {
                    // Q[i] => set of args which h|i maps to
                    Q_builder.get(i).add(t.arguments.get(t_mapped_idx));
                }
            }
            beta = tNorm.apply(beta, proximityRelation.proximity);
            if (beta < lambda) {
                return new Pair<>(null, beta); // should not be dereferenced
            }
        }
        List<ArraySet<GroundTerm>> Q = Util.newList(Q_builder.size(), i -> new ArraySet<>(Q_builder.get(i)));
        return new Pair<>(Q, beta);
    }
    
    private AUT expand(AUT aut, int freshVar) {
        Pair<Queue<GroundTerm>, Integer> T1_conjunction = specialConjunction(aut.T1, freshVar);
        Queue<GroundTerm> C1 = T1_conjunction.left;
        freshVar = T1_conjunction.right;
        
        Pair<Queue<GroundTerm>, Integer> pair2 = specialConjunction(aut.T2, freshVar);
        Queue<GroundTerm> C2 = pair2.left;
        
        assert !C1.isEmpty() && !C2.isEmpty();
        return new AUT(aut.var, C1, C2);
    }
    
    private AUT merge(ArraySet<GroundTerm> T11, ArraySet<GroundTerm> T12, ArraySet<GroundTerm> T21, ArraySet<GroundTerm> T22, int freshVar) {
        Pair<Queue<GroundTerm>, Integer> pair1 = specialConjunction(ArraySet.merged(T11, T12), freshVar);
        Queue<GroundTerm> Q1 = pair1.left;
        freshVar = pair1.right;
        
        if (Q1.isEmpty()) {
            return new AUT(freshVar, Q1, Q1);
        }
        
        Pair<Queue<GroundTerm>, Integer> pair2 = specialConjunction(ArraySet.merged(T21, T22), freshVar);
        Queue<GroundTerm> Q2 = pair2.left;
        freshVar = pair2.right;
        
        return new AUT(freshVar, Q1, Q2);
    }
    
    private Set<Solution> generateSolutions(Collection<Config> configs) {
        Set<Solution> solutions = configs.stream().map(config -> {
            Term r = Substitution.applyAll(config.substitutions, VariableTerm.VAR_0);
            Pair<Witness, Witness> witnesses = generateWitnesses ? generateWitnesses(config, r) : new Pair<>(null, null);
            return new Solution(r, witnesses.left, witnesses.right, config.alpha1, config.alpha2);
        }).collect(Collectors.toSet());
        
        log.info(ANSI.green("SOLUTIONS:"));
        solutions.forEach(solution -> log.info("{}", solution));
        log.info("🧇");
        return solutions;
    }
    
    private Pair<Witness, Witness> generateWitnesses(Config config, Term r) {
        Map<Integer, Set<Term>> W1 = new HashMap<>();
        Map<Integer, Set<Term>> W2 = new HashMap<>();
        for (int var : r.v_named()) {
            Pair<Set<Term>, Set<Term>> applied = AUT.applyAll(config.S, new VariableTerm(var));
            W1.put(var, applied.left);
            W2.put(var, applied.right);
        }
        return new Pair<>(new Witness(W1), new Witness(W2));
    }
    
    // *** special conjunction ***
    
    private boolean consistent(ArraySet<GroundTerm> terms) {
        return runConj(terms, VariableTerm.VAR_0, true) == IS_CONSISTENT;
    }
    
    private Pair<Queue<GroundTerm>, Integer> specialConjunction(ArraySet<GroundTerm> terms, int freshVar) {
        Pair<Queue<GroundTerm>, Integer> result = runConj(terms, freshVar, false);
        assert result != null;
        return result;
    }
    
    private final Pair<Queue<GroundTerm>, Integer> IS_CONSISTENT = new Pair<>(null, null);
    
    private Pair<Queue<GroundTerm>, Integer> runConj(ArraySet<GroundTerm> terms, int freshVar, boolean consistencyCheck) {
        Queue<State> branches = new ArrayDeque<>();
        branches.add(new State(terms, freshVar));
        
        Queue<GroundTerm> solutions = consistencyCheck ? null : new ArrayDeque<>();
        BRANCHING:
        while (!branches.isEmpty()) {
            State state = branches.remove();
            while (!state.expressions.isEmpty()) {
                Expression expression = state.expressions.remove();
                ArraySet<GroundTerm> nonAnonTerms = expression.T.filter(term -> !MappedVariableTerm.ANON.equals(term));
                // REMOVE
                if (consistencyCheck && nonAnonTerms.size() <= 1 || nonAnonTerms.isEmpty()) {
                    state.s.add(new Substitution(expression.var, MappedVariableTerm.ANON));
                    continue;
                }
                // REDUCE
                for (String h : R.commonProximates(nonAnonTerms)) {
                    List<ArraySet<GroundTerm>> Q = map(h, nonAnonTerms, 1.0f).left;
                    assert Q != null;
                    State childState = state.copy();
                    
                    List<Term> h_args = Util.newList(R.arity(h), i -> {
                        int y_i = childState.freshVar();
                        childState.expressions.add(new Expression(y_i, Q.get(i)));
                        return new VariableTerm(y_i);
                    });
                    
                    freshVar = Math.max(freshVar, childState.peekVar());
                    Term h_term = R.isMappedVar(h) ? new MappedVariableTerm(h) : new FunctionTerm(h, h_args);
                    childState.s.add(new Substitution(expression.var, h_term));
                    branches.add(childState);
                }
                continue BRANCHING;
            }
            if (consistencyCheck) {
                return IS_CONSISTENT;
            }
            solutions.add(Substitution.applyAllForceGroundTerm(state.s, state.peekVar()));
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
