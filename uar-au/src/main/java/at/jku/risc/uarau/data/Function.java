package at.jku.risc.uarau.data;

import java.util.ArrayList;
import java.util.List;

public class Function extends Term {
    public final List<Term> arguments;
    
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
}
