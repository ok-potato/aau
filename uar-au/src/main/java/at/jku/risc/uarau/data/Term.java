package at.jku.risc.uarau.data;

import at.jku.risc.uarau.Util;
import org.junit.platform.commons.util.StringUtils;

import java.util.Arrays;

public class Term {
    public static final int UNUSED_VAR = -2;
    public static final Term ANON = new Term(UNUSED_VAR + 1);
    
    public final int var;
    public final String head;
    public final Term[] arguments;
    
    private Integer hash = null;
    
    // function term
    public Term(String head, Term[] arguments) {
        assert (!StringUtils.isBlank(head));
        this.var = UNUSED_VAR;
        this.head = head.intern();
        this.arguments = arguments;
    }
    
    // constant term
    public Term(String head) {
        this(head, new Term[0]);
    }
    
    // variable term
    public Term(int var) {
        assert (var > UNUSED_VAR);
        this.var = var;
        this.head = null;
        this.arguments = null;
    }
    
    public boolean isVar() {
        return var > UNUSED_VAR;
    }
    
    @Override
    public String toString() {
        if (var == ANON.var) {
            return "_";
        }
        if (isVar()) {
            return String.format("%s", var);
        }
        return head + Util.joinString(Arrays.asList(arguments), ",", "()", "(", ")");
    }
    
    @Override
    public int hashCode() {
        if (hash == null) {
            if (isVar()) {
                hash = var;
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
        if (isVar()) {
            return var == otherTerm.var;
        }
        return head.equals(otherTerm.head) && Arrays.equals(arguments, otherTerm.arguments);
    }
    
}
