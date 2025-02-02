package at.jku.risc.uarau;

import at.jku.risc.uarau.impl.Algorithm;
import at.jku.risc.uarau.term.GroundTerm;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Data;
import at.jku.risc.uarau.util.Pair;

import java.util.Collections;
import java.util.Set;

public class TestUtils {
    public static Set<Solution> verify(Problem problem) {
        Algorithm algorithm = new Algorithm(problem);
        Set<Solution> solutions = algorithm.run();
        
        if (problem.wantsWitnesses()) {
            for (Solution solution : solutions) {
                // var(Solution) <=> keys(witnesses)
                Set<Integer> v_named = solution.generalization.v_named();
                assert v_named.equals(solution.lhs.substitutions.keySet())
                        && v_named.equals(solution.rhs.substitutions.keySet());
                
                Pair<Set<GroundTerm>, Set<GroundTerm>> enumerated = solution.enumerate();

                // all substitutions should lead to proximates of the problem terms
                assert Data.all(enumerated.left, groundTerm -> algorithm.consistent(new ArraySet<>(groundTerm, problem.getEquation().left)));
                assert Data.all(enumerated.right, groundTerm -> algorithm.consistent(new ArraySet<>(groundTerm, problem.getEquation().right)));
                
                if (!Collections.disjoint(enumerated.left, enumerated.right)) {
                    // there can be no common terms between left and right - unless we found a fully substituted generalization
                    GroundTerm.force(solution.generalization);
                } else {
                    // there are some substitutions, so check there wasn't a more specific generalization
                    assert Data.none(enumerated.left, groundTerm -> algorithm.consistent(new ArraySet<>(groundTerm, problem.getEquation().right)));
                    assert Data.none(enumerated.right, groundTerm -> algorithm.consistent(new ArraySet<>(groundTerm, problem.getEquation().left)));
                }
            }
        }
        
        return solutions;
    }
    
    public static boolean close(double a, double b) {
        return Math.abs(a - b) < 0.00001f * Math.abs(a + b) / 2;
    }
}
