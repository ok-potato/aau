package at.jku.risc.aau.util;

import java.util.Objects;

/**
 * Utility data structure used for two-valued function returns, particularly for compact representation of LHS + RHS.
 */
public class Pair<L, R> {
    public final L left;
    public final R right;
    
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }
    
    public static <L, R> Pair<L, R> of (L left, R right) {
        return new Pair<>(left, right);
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Pair)) {
            return false;
        }
        Pair<?, ?> other = (Pair<?, ?>) object;
        return Objects.equals(this.left, other.left) && Objects.equals(this.right, other.right);
        
    }
    
    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(left) + Objects.hashCode(right);
    }
    
    @Override
    public String toString() {
        return left + ANSI.yellow(" -- ") + right;
    }
}