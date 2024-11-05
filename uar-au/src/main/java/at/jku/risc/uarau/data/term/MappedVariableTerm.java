package at.jku.risc.uarau.data.term;

import java.util.Collections;

/**
 *
 */
public class MappedVariableTerm extends GroundTerm {
    /**
     * ANON (The anonymous variable) is not described in the paper as a mapped variable -
     * in fact, it is described as a regular variable.
     * <br>
     * However, in this theory, it effectively functions as a constant - since substitutions cannot apply to it -
     * and by defining it as such, we can define all terms which appear in {@linkplain at.jku.risc.uarau.data.AUT}s
     * as {@linkplain GroundTerm}, which eliminates a class of possible bugs.
     * <br><br>
     * <b>Note:</b> Naming a variable '_' in the problem causes no functional issues -
     * see {@linkplain GroundTerm#equals(Object)} - although it might be visually confusing.
     */
    public static final MappedVariableTerm ANON = new MappedVariableTerm("_");
    
    public MappedVariableTerm(String head) {
        super(head, Collections.emptyList());
    }
    
    @Override
    public String toString() {
        return head;
    }
}
