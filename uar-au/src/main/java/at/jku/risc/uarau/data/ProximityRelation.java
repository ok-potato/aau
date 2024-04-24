package at.jku.risc.uarau.data;

import java.util.Arrays;

public record ProximityRelation(String f, String g, float proximity, int[][] argumentRelation) {
    // TODO revisit since we're using strings here now
    public ProximityRelation(String f, String g, float proximity, int[][] argumentRelation) {
        // order by function symbol
        if (f.hashCode() < g.hashCode()) {
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
        return 31 * f.hashCode() + g.hashCode();
    }
    
    private static int[][] flip(int[][] original) {
        int[][] result = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            result[i] = new int[]{original[i][1], original[i][0]};
        }
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int[] map : argumentRelation) {
            sb.append(STR."(\{map[0]},\{map[1]}),");
        }
        return STR."\{f} ~ \{g} [\{proximity}] \{sb.delete(sb.length()-1, sb.length())}";
    }
}
