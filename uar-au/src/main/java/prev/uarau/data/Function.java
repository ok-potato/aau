package prev.uarau.data;

import java.util.ArrayList;
import java.util.List;

public class Function extends Term {
    public final List<Term> arguments;
    private Integer hash = null;
    
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
            sb.append(arguments.get(arguments.size() - 1));
        }
        return String.format("%s(%s)", head, sb);
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Function) {
            Function otherFunction = (Function) other;
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
        if (hash != null) {
            return hash;
        }
        hash = head.hashCode() + 31 * arguments.hashCode();
        return hash;
    }
}
