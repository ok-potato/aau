package at.jku.risc.uarau.data;

import java.util.HashSet;
import java.util.Set;

public class ProximityMap {
    record ProximityRelation(int f, int g, float proximity, int[][] argumentRelation) {
        ProximityRelation(int f, int g, float proximity, int[][] argumentRelation) {
            // order by function symbol
            if (f < g) {
                // default
                this.f = f;
                this.g = g;
                this.argumentRelation = argumentRelation;
            } else {
                // reversed
                this.f = g;
                this.g = f;
                this.argumentRelation = flip(argumentRelation);
            }
            this.proximity = proximity;
        }
        
        @Override
        public boolean equals(Object other) {
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            ProximityRelation that = (ProximityRelation) other;
            return f == that.f && g == that.g;
        }
        
        @Override
        public int hashCode() {
            return 31 * f + g;
        }
    }
    
    private final Set<ProximityRelation> map = new HashSet<>();
    
    public void add(int f, int g, float proximity, int[][] argumentRelation) {
        // ASSERT
        
        for (int[] pair : argumentRelation) {
            assert (pair.length == 2);
        }
        var relation = new ProximityRelation(f, g, proximity, argumentRelation);
    }
    
    private static int[][] flip(int[][] original) {
        int[][] result = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = new int[]{original[i][1], original[i][0]};
        }
        return result;
    }
}
