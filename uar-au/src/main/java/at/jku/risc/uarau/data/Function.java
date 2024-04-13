package at.jku.risc.uarau.data;

public class Function extends Term {
    
    public Function(int head) {
        super(head);
    }
    
    public static Function fresh() {
        return new Function(fresh--);
    }
}
