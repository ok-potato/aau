package at.jku.risc.aau;

public interface TNorm {
    /**
     * Implementation of this function must follow the definition of the <a href="https://en.wikipedia.org/wiki/T-norm">triangular norm</a>
     * <br><br>
     * that is, a function <b>[0,1]x[0,1]->[0,1]</b> that satisfies
     * <br>
     * <b>commutativity</b>, <b>monotonicity</b> and <b>associativity</b>, with <b>1 as the identity</b>.
     * <br><br>
     * The default implementation is {@linkplain Math#min(float, float)}
     */
    float apply(float a, float b);
}
