package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.DataUtil;
import org.junit.platform.commons.util.StringUtils;

import java.util.*;

public class Term {
    public static final int UNUSED_VAR = -2;
    public static final Term ANON = new Term(UNUSED_VAR + 1);
    public static final int VAR_0 = UNUSED_VAR + 2;
    
    public final int var;
    public final String head;
    public final List<Term> arguments;
    
    public final boolean mappedVar;
    
    // function/constant term
    public Term(String head, List<Term> arguments) {
        assert !StringUtils.isBlank(head);
        this.var = UNUSED_VAR;
        this.head = head.intern();
        if (arguments == null) {
            this.arguments = Collections.emptyList();
            mappedVar = true;
        } else {
            this.arguments = Collections.unmodifiableList(arguments);
            mappedVar = false;
        }
    }
    
    public Term(String head, Term[] arguments) {
        this(head, Arrays.asList(arguments));
    }
    
    // mapped variable term
    public Term(String head) {
        this(head, (List<Term>) null);
    }
    
    // variable term
    public Term(int var) {
        assert var != UNUSED_VAR + 1 || this != Term.ANON;
        this.var = var;
        this.head = null;
        this.arguments = null;
        mappedVar = false;
    }
    
    private Integer depth = null;
    
    public int depth() {
        if (depth == null) {
            if (arguments == null || arguments.isEmpty()) {
                depth = 0;
            } else {
                depth = 1 + arguments.stream().map(Term::depth).max(Integer::compare).orElse(0);
            }
        }
        return depth;
    }
    
    private Set<Integer> V = null;
    
    public Set<Integer> V_named() {
        if (V == null) {
            V = new HashSet<>();
            if (arguments != null) {
                for (Term argument : arguments) {
                    V.addAll(argument.V_named());
                }
            } else if (this != ANON) {
                V.add(var);
            }
        }
        return V;
    }
    
    public boolean isVar() {
        return var > UNUSED_VAR;
    }
    
    private Integer hash = null;
    
    @Override
    public int hashCode() {
        if (hash == null) {
            if (isVar()) {
                hash = var;
            } else {
                hash = head.hashCode() + 31 * arguments.hashCode();
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
        if (isVar()) {
            return var == otherTerm.var;
        }
        if (hashCode() != otherTerm.hashCode()) {
            return false;
        }
        return head.equals(otherTerm.head) && arguments.equals(otherTerm.arguments);
    }
    
    @Override
    public String toString() {
        return toString(true);
    }
    
    public String toString(boolean ansi) {
        if (var == ANON.var) {
            return "_";
        }
        if (isVar()) {
            return ansi ? ANSI.yellow(var) : String.valueOf(var);
        }
        if (mappedVar) {
            return head;
        }
        return head + DataUtil.str(arguments, ",", "()", "(", ")");
    }
}
