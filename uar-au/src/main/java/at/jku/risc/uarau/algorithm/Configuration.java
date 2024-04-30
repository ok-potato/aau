package at.jku.risc.uarau.algorithm;

import at.jku.risc.uarau.data.AUT;
import at.jku.risc.uarau.data.Term;
import at.jku.risc.uarau.data.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Configuration {
    // Tuple (A; S; r; alpha1; alpha2) - used to track state
    final Set<AUT> unsolved, solved; // 'A', 'S'
    Variable generalization; // 'r'
    Map<Variable, Term> substitution; // var -> term
    float alpha1, alpha2;
    
    int freshName;
    
    Configuration() {
        unsolved = new HashSet<>(); // TODO do I need unsolved?
        solved = new HashSet<>();
        substitution = new HashMap<>();
        generalization = freshVar();
        alpha1 = 1.0f;
        alpha2 = 1.0f;
    }
    
    Configuration(Configuration cfg) {
        unsolved = new HashSet<>(cfg.unsolved);
        solved = new HashSet<>(cfg.solved);
        generalization = cfg.generalization;
        substitution = new HashMap<>(cfg.substitution);
        alpha1 = cfg.alpha1;
        alpha2 = cfg.alpha2;
        freshName = cfg.freshName;
    }
    
    public Variable freshVar() {
        return new Variable(String.valueOf(freshName++));
    }
}