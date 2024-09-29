package at.jku.risc.uarau;

import at.jku.risc.uarau.data.*;
import at.jku.risc.uarau.data.term.*;
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
            throw Util.argException("Lambda must be in range [0,1]");
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
        log.info(R.restriction.correspondence ?
                "Therefore, there are no irrelevant positions, and we get the minimal complete set of generalizations" :
                "Therefore, there might be irrelevant positions, and we are not guaranteed to get the minimal complete set of generalizations");
        
        // *** APPLY RULES ***
        Queue<Config> linearConfigs = new ArrayDeque<>();
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
            linearConfigs.add(config);
        }
        
        assert Util.allUnique(linearConfigs);
        if (!merge && !generateWitnesses) {
            return generateSolutions(linearConfigs);
        }
        
        // *** POST PROCESS ***
        log.info(ANSI.green("LINEAR:") + "{}{}", LOG_NEWLINE, Util.str(linearConfigs, LOG_NEWLINE, ""));
        // EXPAND
        Queue<Config> expandedConfigs = Util.mapQueue(linearConfigs, config -> config.mapSolutions(this::expand));
        assert Util.allUnique(expandedConfigs);
        if (!merge) {
            return generateSolutions(expandedConfigs);
        }
        log.info(ANSI.green("EXPANDED:") + "{}{}", LOG_NEWLINE, Util.str(expandedConfigs, LOG_NEWLINE, ""));
        // MERGE
        Queue<Config> mergedSolutions = Util.mapQueue(expandedConfigs, config -> config.mapSolutions(this::merge));
        assert Util.allUnique(mergedSolutions);
        return generateSolutions(mergedSolutions);
    }
    
    private Queue<Config> decompose(AUT aut, Config cfg) {
        Queue<Config> children = new ArrayDeque<>();
        for (String h : R.commonProximates(ArraySet.merged(aut.T1, aut.T2))) {
            // map args
            Pair<List<ArraySet<GroundTerm>>, Float> T1Mapped = mapArgs(h, aut.T1, cfg.alpha1);
            List<ArraySet<GroundTerm>> Q1 = T1Mapped.left;
            float alpha1 = T1Mapped.right;
            if (alpha1 < lambda) {
                continue;
            }
            Pair<List<ArraySet<GroundTerm>>, Float> T2Mapped = mapArgs(h, aut.T2, cfg.alpha2);
            List<ArraySet<GroundTerm>> Q2 = T2Mapped.left;
            float alpha2 = T2Mapped.right;
            if (alpha2 < lambda) {
                continue;
            }
            assert Q1 != null && Q2 != null;
            if (!R.restriction.mapping && (Util.any(Q1, this::notConsistent) || Util.any(Q2, this::notConsistent))) {
                continue;
            }
            // apply DEC
            Config child = cfg.copy();
            child.alpha1 = alpha1;
            child.alpha2 = alpha2;
            List<Term> hArgs = Util.newList(R.arity(h), i -> {
                int yi = child.freshVar();
                child.A.add(new AUT(yi, Q1.get(i), Q2.get(i)));
                return new VariableTerm(yi);
            });
            Term hTerm = R.isMappedVar(h) ? new MappedVariableTerm(h) : new FunctionTerm(h, hArgs);
            child.substitutions.add(new Substitution(aut.var, hTerm));
            children.add(child);
        }
        return children;
    }
    
    /**
     * for each <b>t</b> in <b>T</b>, add the arguments which <b>h|i</b> maps to, to <b>Q[i]</b>
     * <br><br>
     * <code>
     * e.g. given .... T = {f(a,b), g(c)}
     * <br>
     * .... with ..... h -> f {(1,1),(2,1)} ,  h -> g {(2,1)}
     * <br>
     * .... then ..... Q = [{a}, {b,c}]
     * </code>
     */
    private Pair<List<ArraySet<GroundTerm>>, Float> mapArgs(String h, ArraySet<GroundTerm> T, float beta) {
        int hArity = R.arity(h);
        List<Set<GroundTerm>> Q = Util.newList(hArity, i -> new HashSet<>());
        for (GroundTerm t : T) {
            ProximityRelation h_t_Relation = R.proximityRelation(h, t.head);
            beta = tNorm.apply(beta, h_t_Relation.proximity);
            if (beta < lambda) {
                return new Pair<>(null, beta);
            }
            for (int hIdx = 0; hIdx < hArity; hIdx++) {
                for (int termIdx : h_t_Relation.argRelation.get(hIdx)) {
                    Q.get(hIdx).add(t.arguments.get(termIdx));
                }
            }
        }
        return new Pair<>(Util.mapList(Q, ArraySet::new), beta);
    }
    
    private Queue<AUT> expand(Config linearCfg) {
        final int freshVar = linearCfg.freshVar();
        
        return Util.mapQueue(linearCfg.S, aut -> {
            Pair<ArraySet<GroundTerm>, Integer> E1 = specialConjunction(aut.T1, freshVar);
            Pair<ArraySet<GroundTerm>, Integer> E2 = specialConjunction(aut.T2, E1.right);
            assert !E1.left.isEmpty() && !E2.left.isEmpty();
            return new AUT(aut.var, E1.left, E2.left);
        });
    }
    
    private Queue<AUT> merge(Config expandedCfg) {
        Queue<AUT> remaining = new ArrayDeque<>(expandedCfg.S);
        Queue<AUT> result = new ArrayDeque<>();
        
        while (!remaining.isEmpty()) {
            // pick one AUT as 'collector'
            AUT collector = remaining.remove();
            Queue<AUT> notCollected = new ArrayDeque<>();
            Queue<Integer> collectedVars = new ArrayDeque<>();
            // need to manually keep track of 'fresh var'
            int freshVar = expandedCfg.peekVar();
            // try merge on each remaining
            for (AUT candidate : remaining) {
                Pair<ArraySet<GroundTerm>, Integer> mergedLHS = specialConjunction(ArraySet.merged(collector.T1, candidate.T1), freshVar);
                Pair<ArraySet<GroundTerm>, Integer> mergedRHS = mergedLHS.left.isEmpty() ? mergedLHS :
                        specialConjunction(ArraySet.merged(collector.T2, candidate.T2), mergedLHS.right);
                
                if (mergedLHS.left.isEmpty() || mergedRHS.left.isEmpty()) {
                    notCollected.add(candidate);
                } else {
                    collectedVars.add(candidate.var);
                    collector = new AUT(collector.var, mergedLHS.left, mergedRHS.left);
                    freshVar = mergedRHS.right;
                }
            }
            // we're done with the merged AUTs, since they can't be consistent with any remaining merges
            remaining = notCollected;
            if (collectedVars.isEmpty()) {
                result.add(collector);
            } else {
                collectedVars.add(collector.var);
                final VariableTerm y = new VariableTerm(freshVar);
                collectedVars.forEach(var -> expandedCfg.substitutions.add(new Substitution(var, y)));
                result.add(new AUT(y.var, collector.T1, collector.T2));
            }
        }
        assert Util.allUnique(result);
        return result;
    }
    
    private Set<Solution> generateSolutions(Collection<Config> configs) {
        Set<Solution> solutions = configs.stream().map(config -> {
            Term r = Substitution.applyAll(config.substitutions, VariableTerm.VAR_0);
            Pair<Witness, Witness> witnesses =
                    generateWitnesses ? generateWitnesses(config, r) : new Pair<>(null, null);
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
    
    private boolean notConsistent(ArraySet<GroundTerm> terms) {
        return runConj(terms, VariableTerm.VAR_0, true) != IS_CONSISTENT;
    }
    
    private Pair<ArraySet<GroundTerm>, Integer> specialConjunction(ArraySet<GroundTerm> terms, int freshVar) {
        Pair<ArraySet<GroundTerm>, Integer> result = runConj(terms, freshVar, false);
        assert result != null;
        return result;
    }
    
    private final Pair<ArraySet<GroundTerm>, Integer> IS_CONSISTENT = new Pair<>(null, null);
    
    private Pair<ArraySet<GroundTerm>, Integer> runConj(ArraySet<GroundTerm> terms, int freshVar, boolean consistencyCheck) {
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
                    List<ArraySet<GroundTerm>> Q = mapArgs(h, nonAnonTerms, 1.0f).left;
                    assert Q != null;
                    State childState = state.copy();
                    
                    List<Term> h_args = Util.newList(R.arity(h), i -> {
                        int yi = childState.freshVar();
                        childState.expressions.add(new Expression(yi, Q.get(i)));
                        return new VariableTerm(yi);
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
        return new Pair<>(new ArraySet<>(solutions, true), freshVar);
    }
}
