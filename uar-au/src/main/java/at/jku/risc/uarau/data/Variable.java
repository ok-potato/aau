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
}
