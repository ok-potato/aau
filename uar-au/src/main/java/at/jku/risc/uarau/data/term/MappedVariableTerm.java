package at.jku.risc.uarau.data.term;

import java.util.Collections;

public class MappedVariableTerm extends GroundTerm {
    public static final MappedVariableTerm ANON = new MappedVariableTerm("_");
    
    public MappedVariableTerm(String head) {
        super(head, Collections.emptyList());
    }
    
    @Override
    public String toString() {
        return head;
    }
}
