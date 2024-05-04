package at.jku.risc.uarau.data;

import java.util.ArrayList;
import java.util.List;

public class Function extends Term {
    public final List<Term> arguments;
    private int hash = 0;
    
    public Function(String head, List<Term> arguments) {
        super(head);
        this.arguments = arguments;
    }
    
    public Function(String head) {
        super(head);
        this.arguments = new ArrayList<>();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.size() - 1; i++) {
            sb.append(arguments.get(i)).append(",");
        }
        if (!arguments.isEmpty()) {
            sb.append(arguments.getLast());
        }
        return STR."\{head}(\{sb.toString()})";
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Function otherFunction) {
            // this is okay fast, but could also do pooling for more fast
            if (hashCode() != otherFunction.hashCode()) {
                return false;
            }
            return this.head == otherFunction.head && arguments.equals(otherFunction.arguments);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = head.hashCode() + 31 * arguments.hashCode();
            if (hash == 0) {
                System.out.println("Function got hashed as 0; this shouldn't happen very often.");
                hash = 1234567890;
            }
        }
        return hash;
    }
}
