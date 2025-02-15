package at.jku.risc.aau.term;

import java.util.List;

/**
 * Implementation of {@linkplain GroundishTerm}, which can take GroundishTerms as its arguments.
 * <br>
 * This could be implemented as a union type if that concept existed in Java.
 */
public class GroundishTermImpl extends GroundishTerm {
    private final List<GroundishTerm> arguments;

    public GroundishTermImpl(String head, List<GroundishTerm> arguments) {
        super(head);
        this.arguments = arguments;
    }

    @Override
    public List<GroundishTerm> arguments() {
        return arguments;
    }
}
