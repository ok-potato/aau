package at.jku.risc.uarau.data;

import java.util.*;

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
        if (hash != null) {
            return hash;
        }
        hash = head.hashCode() + 31 * arguments.hashCode();
        return hash;
    }
    
    public void foo(){
        SequencedSet<String> E = new LinkedHashSet<>();
        E.add("asdf");
        E.add("qwer");
        E.add("zxcv");
        E.add("tyui");
        while (!E.isEmpty()) {
            String a = E.getFirst();
            Set<String> joined = new HashSet<>();
            for (String e : E) {
                if (e.startsWith(a.substring(0, 2))) {
                    a += e;
                    joined.add(e);
                }
            }
            E.removeAll(joined);
        }
    }
}
