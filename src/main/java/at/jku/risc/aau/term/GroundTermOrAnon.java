package at.jku.risc.aau.term;

import java.util.List;

/**
 * Implementation of {@linkplain GroundishTerm}, which can take Groundish Terms
 * (with no variable sub-terms besides ANON) as its arguments.
 * <br>
 * This could be implemented as a union type if that concept existed in Java.
 */
public class GroundTermOrAnon extends GroundishTerm {
    private final List<GroundishTerm> arguments;

    public GroundTermOrAnon(String head, List<GroundishTerm> arguments) {
        super(head);
        this.arguments = arguments;
    }

    @Override
    public List<GroundishTerm> arguments() {
        return arguments;
    }
}
