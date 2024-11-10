package at.jku.risc.uarau;

import at.jku.risc.uarau.data.*;
import at.jku.risc.uarau.data.term.*;
import at.jku.risc.uarau.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core implementation of the Algorithm described in the paper:
 * <br>
 * <a href="https://doi.org/10.1007/978-3-031-10769-6_34">A Framework for Approximate Generalization in Quantitative Theories</a>
 * <br><br>
 * You can run the Algorithm by defining a {@linkplain Problem}, and calling {@linkplain Problem#solve()} (or {@linkplain Algorithm#solve(Problem)})
 * <br>
 * or directly calling {@linkplain Algorithm#solve(String, String, float)} with some string input
 */
public class Algorithm {
    // *** api ***
    
    /**
     * Convenience method for simple string-based inputs
     * <br>
     * For more complex queries, use {@linkplain Problem#solve()}
     */
    public static Set<Solution> solve(String equation, String proximityRelations, float lambda) {
        Problem problem = new Problem(Parser.parseEquation(equation));
        return solve(problem.proximityRelations(Parser.parseProximityRelations(proximityRelations)).lambda(lambda));
    }
    
    /**
     * Gets called by {@linkplain Problem#solve()}, but you can also call it directly, if that's how you like doing things
     */
    public static Set<Solution> solve(Problem problem) {
        return new Algorithm(problem).run();
    }
    
    // ^^^ api ^^^
    
    // *** implementation ***
    
    private final Logger log = LoggerFactory.getLogger(Algorithm.class);
    
    private final GroundTerm lhs, rhs;
    private final ProblemMap problemMap;
    private final TNorm tNorm;
    private final float lambda;
    private final boolean doMerge, giveWitnesses;
    
    private Algorithm(Problem problem) {
        lhs = problem.getEquation().left;
        rhs = problem.getEquation().right;
        lambda = problem.getLambda();
        if (lambda < 0.0f || lambda > 1.0f) {
            throw Panic.arg("Lambda must be in range [0,1]");
        }
        problemMap = new ProblemMap(lhs, rhs, problem.getProximityRelations(), lambda);
        tNorm = problem.getTNorm();
        doMerge = problem.doMerge();
        giveWitnesses = problem.giveWitnesses();
    }
    
    private Set<Solution> run() {
        log.info(ANSI.yellow("SOLVING: ") + lhs + ANSI.yellow(" == ") + rhs + ANSI.yellow(" λ=", lambda));
        
        if (log.isDebugEnabled()) {
            log.debug(Util.log(ANSI.yellow("R:"), problemMap.fullView()));
        } else {
            log.info(ANSI.yellow("R: ") + Util.str(problemMap.compactView()));
        }
        
        if (problemMap.restrictionType == problemMap.theoreticalRestrictionType) {
            log.info("The problem is of type {}.", ANSI.blue(problemMap.restrictionType));
        } else {
            log.info("The problem is in theory of type {}. But excluding relations below the λ-cut, it is of type {}.",
                    ANSI.blue(problemMap.theoreticalRestrictionType),
                    ANSI.blue(problemMap.restrictionType));
        }
        log.info(problemMap.restrictionType.correspondence ?
                "Therefore, we get the minimal complete set of generalizations." :
                "Therefore, we are not guaranteed to get the minimal complete set of generalizations.");
        
        // *** APPLY RULES ***
        Queue<Config> linearConfigs = new ArrayDeque<>();
        Queue<Config> branches = new ArrayDeque<>();
        branches.add(new Config(lhs, rhs));
        
        BRANCHING:
        while (!branches.isEmpty()) {
            assert Util.isSet(branches);
            Config cfg = branches.remove();
            while (!cfg.A.isEmpty()) {
                AUT aut = cfg.A.remove();
                // TRIVIAL
                if (aut.T1.isEmpty() && aut.T2.isEmpty()) {
                    cfg.substitutions.add(new Substitution(aut.variable, MappedVariableTerm.ANON));
                    log.debug("TRI => {}", cfg);
                    continue;
                }
                // DECOMPOSE
                Queue<Config> children = decompose(aut, cfg);
                if (!children.isEmpty()) {
                    branches.addAll(children);
                    if (log.isDebugEnabled()) {
                        log.debug("DEC => {}", Util.str(children));
                    }
                    continue BRANCHING;
                }
                // SOLVE
                cfg.S.add(aut);
                log.debug("SOL => {}", cfg);
            }
            linearConfigs.add(cfg);
        }
        
        assert Util.isSet(linearConfigs);
        if (!doMerge && !giveWitnesses) {
            return generateSolutions(linearConfigs);
        }
        
        // *** POST PROCESS ***
        log.info(Util.log(ANSI.yellow("LINEAR:"), linearConfigs));
        
        // EXPAND
        Queue<Config> expandedConfigs = Util.mapQueue(linearConfigs, this::expand);
        assert Util.isSet(expandedConfigs);
        if (!doMerge) {
            return generateSolutions(expandedConfigs);
        }
        log.info(Util.log(ANSI.yellow("EXPANDED:"), expandedConfigs));
        
        // MERGE
        Queue<Config> mergedConfigs = Util.mapQueue(expandedConfigs, this::merge);
        assert Util.isSet(mergedConfigs);
        return generateSolutions(mergedConfigs);
    }
    
    private Queue<Config> decompose(AUT aut, Config cfg) {
        Queue<Config> children = new ArrayDeque<>();
        ArraySet<String> commonProximates = problemMap.commonProximates(ArraySet.merged(aut.T1, aut.T2));
        for (String h : commonProximates) {
            // map arguments
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
            if (!problemMap.restrictionType.mapping) {
                if (Util.any(Q1, this::inconsistent) || Util.any(Q2, this::inconsistent)) {
                    continue;
                }
            }
            // apply DEC
            Config child = commonProximates.size() == 1 ? cfg : cfg.copy();
            child.alpha1 = alpha1;
            child.alpha2 = alpha2;
            List<Term> hArgs = Util.list(problemMap.arity(h), idx -> {
                int yi = child.freshVar();
                child.A.add(new AUT(yi, Q1.get(idx), Q2.get(idx)));
                return new VariableTerm(yi);
            });
            Term hTerm = problemMap.isMappedVar(h) ? new MappedVariableTerm(h) : new FunctionTerm(h, hArgs);
            child.substitutions.add(new Substitution(aut.variable, hTerm));
            children.add(child);
        }
        return children;
    }
    
    /**
     * for each <b>t</b> in <b>T</b>, add to <b>Q[i]</b> the arguments which <b>h|i</b> maps to
     * <br><br>
     * <code>
     * ex.: given ... T = {f(a,b), g(c)}
     * <br>
     * .... with .... h -> f {(1,1),(2,1)} ,  h -> g {(2,1)}
     * <br>
     * .... then .... Q = [{a}, {b,c}]
     * </code>
     */
    private Pair<List<ArraySet<GroundTerm>>, Float> mapArgs(String h, ArraySet<GroundTerm> T, float beta) {
        int hArity = problemMap.arity(h);
        List<Set<GroundTerm>> Q = Util.list(hArity, idx -> new HashSet<>());
        for (GroundTerm t : T) {
            ProximityRelation htRelation = problemMap.proximityRelation(h, t.head);
            beta = tNorm.apply(beta, htRelation.proximity);
            if (beta < lambda) {
                return new Pair<>(null, beta);
            }
            for (int hIdx = 0; hIdx < hArity; hIdx++) {
                for (int termIdx : htRelation.argRelation.get(hIdx)) {
                    Q.get(hIdx).add(t.arguments.get(termIdx));
                }
            }
        }
        return new Pair<>(Util.mapList(Q, ArraySet::new), beta);
    }
    
    private Config expand(Config linearCfg) {
        final int freshVar = linearCfg.freshVar();
        
        Queue<AUT> expanded = Util.mapQueue(linearCfg.S, aut -> {
            Pair<ArraySet<GroundTerm>, Integer> E1 = conjunction(aut.T1, freshVar);
            Pair<ArraySet<GroundTerm>, Integer> E2 = conjunction(aut.T2, E1.right);
            assert !E1.left.isEmpty() && !E2.left.isEmpty();
            return new AUT(aut.variable, E1.left, E2.left);
        });
        return linearCfg.copyWithNewS(expanded);
    }
    
    private Config merge(Config expandedCfg) {
        Queue<AUT> remaining = new ArrayDeque<>(expandedCfg.S);
        Queue<AUT> merged = new ArrayDeque<>();
        
        while (!remaining.isEmpty()) {
            // pick one AUT as 'collector'
            AUT collector = remaining.remove();
            Queue<AUT> notCollected = new ArrayDeque<>();
            Queue<Integer> collectedVars = new ArrayDeque<>();
            // need to manually keep track of 'fresh var'
            int freshVar = expandedCfg.peekVar();
            // try merge on each remaining AUT
            for (AUT candidate : remaining) {
                Pair<ArraySet<GroundTerm>, Integer> mergedLHS =
                        conjunction(ArraySet.merged(collector.T1, candidate.T1), freshVar);
                Pair<ArraySet<GroundTerm>, Integer> mergedRHS = mergedLHS.left.isEmpty() ? mergedLHS :
                        conjunction(ArraySet.merged(collector.T2, candidate.T2), mergedLHS.right);
                
                if (mergedLHS.left.isEmpty() || mergedRHS.left.isEmpty()) {
                    notCollected.add(candidate);
                } else {
                    collectedVars.add(candidate.variable);
                    collector = new AUT(collector.variable, mergedLHS.left, mergedRHS.left);
                    freshVar = mergedRHS.right;
                }
            }
            // the merged set can't be consistent with any remaining AUTs, since we checked against them with a subset
            remaining = notCollected;
            if (collectedVars.isEmpty()) {
                merged.add(collector);
            } else {
                collectedVars.add(collector.variable);
                final VariableTerm y = new VariableTerm(freshVar);
                collectedVars.forEach(var -> expandedCfg.substitutions.add(new Substitution(var, y)));
                merged.add(new AUT(y.var, collector.T1, collector.T2));
            }
        }
        assert Util.isSet(merged);
        return expandedCfg.copyWithNewS(merged);
    }
    
    private Set<Solution> generateSolutions(Collection<Config> configs) {
        Set<Solution> solutions = configs.stream().map(cfg -> {
            Term term = Substitution.applyAll(cfg.substitutions, VariableTerm.VAR_0);
            Pair<Witness, Witness> witnesses = giveWitnesses ? generateWitnesses(cfg, term) : new Pair<>(null, null);
            return new Solution(term, witnesses.left, witnesses.right, cfg.alpha1, cfg.alpha2);
        }).collect(Collectors.toSet());
        
        log.info(Util.log(ANSI.yellow("SOLUTIONS:"), solutions));
        log.info("🧇");
        return solutions;
    }
    
    private Pair<Witness, Witness> generateWitnesses(Config cfg, Term r) {
        Map<Integer, Set<Term>> W1 = new HashMap<>();
        Map<Integer, Set<Term>> W2 = new HashMap<>();
        for (int var : r.v_named()) {
            Pair<Set<Term>, Set<Term>> applied = AUT.applyAll(cfg.S, new VariableTerm(var));
            W1.put(var, applied.left);
            W2.put(var, applied.right);
        }
        return new Pair<>(new Witness(W1), new Witness(W2));
    }
    
    // *** special conjunction ***
    
    private boolean inconsistent(ArraySet<GroundTerm> terms) {
        return runConj(terms, VariableTerm.VAR_0, true) != IS_CONSISTENT;
    }
    
    private Pair<ArraySet<GroundTerm>, Integer> conjunction(ArraySet<GroundTerm> terms, int freshVar) {
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
                // by explicitly ignore ANON, we don't need to worry about defining R.proximityClass(ANON)
                // we might also get to "cheat" and apply REMOVE where we couldn't otherwise
                ArraySet<GroundTerm> nonAnonTerms = expression.T.filter(term -> !MappedVariableTerm.ANON.equals(term));
                // REMOVE
                if (consistencyCheck && nonAnonTerms.size() <= 1) {
                    continue;
                } else if (nonAnonTerms.isEmpty()) {
                    state.s.add(new Substitution(expression.var, MappedVariableTerm.ANON));
                    continue;
                }
                // REDUCE
                ArraySet<String> commonProximates = problemMap.commonProximates(nonAnonTerms);
                for (String h : commonProximates) {
                    List<ArraySet<GroundTerm>> Q = mapArgs(h, nonAnonTerms, 1.0f).left;
                    assert Q != null;
                    State childState = commonProximates.size() == 1 ? state : state.copy();
                    
                    List<Term> hArgs = Util.list(problemMap.arity(h), idx -> {
                        int yi = childState.freshVar();
                        childState.expressions.add(new Expression(yi, Q.get(idx)));
                        return new VariableTerm(yi);
                    });
                    
                    freshVar = Math.max(freshVar, childState.peekVar());
                    Term hTerm = problemMap.isMappedVar(h) ? new MappedVariableTerm(h) : new FunctionTerm(h, hArgs);
                    if (!consistencyCheck) {
                        childState.s.add(new Substitution(expression.var, hTerm));
                    }
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
