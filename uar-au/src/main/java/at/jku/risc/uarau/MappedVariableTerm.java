package at.jku.risc.uarau;

import java.util.Collections;

public class MappedVariableTerm extends GroundTerm {
    public static final MappedVariableTerm ANON = new MappedVariableTerm("_");
    
    public MappedVariableTerm(String head) {
        // TODO enforce no anon in input
        super(head, Collections.emptyList());
    }
    
    @Override
    public String toString() {
        return head;
    }
}
