package at.jku.risc.uarau;

import org.junit.platform.commons.util.StringUtils;

import java.util.Arrays;

public class Term {
    public static final Term ANON = new Term(-1);
    
    public final int var;
    public final String head;
    public final Term[] arguments;
    private Integer hash = null;
    
    public Term(String head, Term[] arguments) {
        assert (!StringUtils.isBlank(head));
        this.var = -2;
        this.head = head;
        this.arguments = arguments;
    }
    
    public Term(String head) {
        this(head, new Term[0]);
    }
    
    public Term(int var) {
        assert (var > -2);
        this.var = var;
        this.head = null;
        this.arguments = null;
    }
    
    @Override
    public int hashCode() {
        if (hash == null) {
            if (var > -2) {
                hash = Integer.hashCode(var);
            } else {
                hash = head.hashCode() + 31 * Arrays.hashCode(arguments);
            }
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Term)) {
            return false;
        }
        Term otherTerm = (Term) other;
        if (hashCode() != otherTerm.hashCode()) {
            return false;
        }
        if (var > -2) {
            return var == otherTerm.var;
        }
        return head.equals(otherTerm.head) && Arrays.equals(arguments, otherTerm.arguments);
    }
}
