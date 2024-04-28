package at.jku.risc.uarau.algorithm;

import at.jku.risc.uarau.data.AUT;
import at.jku.risc.uarau.data.Term;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Configuration {
    // Tuple (A; S; r; alpha1; alpha2) - used to track state
    final Set<AUT> unsolved, solved; // 'A', 'S'
    int generalization; // 'r'
    Map<Integer, Term> substitution; // var -> term
    float alpha1, alpha2;
    
    // for fresh variable naming
    int varCounter;
    
    Configuration(int generalization) {
        unsolved = new HashSet<>();
        solved = new HashSet<>();
        substitution = new HashMap<>();
        
        this.generalization = generalization;
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
        varCounter = cfg.varCounter;
    }
}