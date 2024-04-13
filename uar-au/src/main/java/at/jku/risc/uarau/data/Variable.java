package at.jku.risc.uarau.data;

public class Variable extends Term {
    
    public Variable(int head) {
        super(head);
    }
    
    public static Variable fresh() {
        return new Variable(fresh--);
    }
}
