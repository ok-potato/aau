package at.jku.risc.uarau.data;

import at.jku.risc.uarau.Util;
import org.junit.platform.commons.util.StringUtils;

import java.util.Arrays;

public class Term {
    public static final int UNUSED_VAR = -2;
    public static final Term ANON = new Term(UNUSED_VAR + 1);
    public static final int VAR_0 = UNUSED_VAR + 2;
    
    public final int var;
    public final String head;
    public final Term[] arguments;
    
    public final boolean mappedVar;
    private Integer depth = null;
    private Integer hash = null;
    
    // function/constant term
    public Term(String head, Term[] arguments) {
        assert (!StringUtils.isBlank(head));
        this.var = UNUSED_VAR;
        this.head = head.intern();
        if (arguments == null) {
            this.arguments = new Term[0];
            mappedVar = true;
        } else {
            this.arguments = arguments;
            mappedVar = false;
        }
    }
    
    // mapped variable term
    public Term(String head) {
        this(head, null);
    }
    
    // variable term
    public Term(int var) {
        assert (var > UNUSED_VAR);
        this.var = var;
        this.head = null;
        this.arguments = null;
        mappedVar = false;
    }
    
    public int depth() {
        if (depth == null) {
            if (arguments == null || arguments.length == 0) {
                depth = 0;
            } else {
                depth = 1 + Arrays.stream(arguments).map(Term::depth).max(Integer::compare).orElse(0);
            }
        }
        return depth;
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
        if (mappedVar) {
            return head;
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
