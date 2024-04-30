package at.jku.risc.uarau.data;

public class Variable extends Term {
    public static final Variable ANON = new Variable("_");
    
    public Variable(String head) {
        super(head);
    }
    
    @Override
    public String toString() {
        return head;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof Variable otherVariable) {
            return this.head == otherVariable.head;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return head.hashCode();
    }
}
