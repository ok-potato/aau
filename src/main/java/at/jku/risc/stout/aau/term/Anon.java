package at.jku.risc.stout.aau.term;

import at.jku.risc.stout.aau.Solution;

import java.util.Collections;
import java.util.List;

/**
 * ANON (the anonymous variable) marks "irrelevant positions" in the
 * {@linkplain Solution#generalization Solution.generalizations},
 * i.e. positions where any term can serve as a substitution without effecting the generalization's proximity.
 */
public class Anon extends GroundishTerm {
    public static final Anon ANON = new Anon();

    private Anon() {
        super("_");
    }

    @Override
    public List<? extends GroundishTerm> arguments() {
        return Collections.emptyList();
    }
}
