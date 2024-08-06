package at.jku.risc.uarau.util;

public final class Quad<A, B, C, D> {
    public A a;
    public B b;
    public C c;
    public D d;
    
    public Quad(A a, B b, C c, D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
}