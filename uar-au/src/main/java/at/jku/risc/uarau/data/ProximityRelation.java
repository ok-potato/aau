package at.jku.risc.uarau.data;

import java.util.Arrays;

public class ProximityRelation {
    
    public String f, g;
    public float proximity;
    int[][] argumentRelation;
    Integer hash = null;
    
    public ProximityRelation(String f, String g, float proximity, int[][] argumentRelation) {
        for (int[] pair : argumentRelation) {
            if (pair.length != 2) {
                throw new IllegalArgumentException();
            }
        }
        // do fixed ordering
        if (f.hashCode() < g.hashCode()) {
            this.f = f.intern();
            this.g = g.intern();
            this.argumentRelation = argumentRelation;
        } else {
            this.f = g.intern();
            this.g = f.intern();
            this.argumentRelation = reverseMapping(argumentRelation);
        }
        this.proximity = proximity;
    }
    
    public String getOther(String f) {
        if (f == this.f) {
            return this.g;
        }
        if (f == this.g) {
            return this.f;
        }
        throw new IllegalArgumentException(String.format("Called getOther on ProximityRelation %s with function name %s!", this, f));
    }
    
    @Override
    public int hashCode() {
        if (hash != null) {
            return hash;
        }
        hash = 961 * f.hashCode() + 31 * g.hashCode() + Arrays.deepHashCode(argumentRelation);
        return hash;
    }
    
    private static int[][] reverseMapping(int[][] original) {
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
            sb.append(String.format("(%d,%d),", map[0], map[1]));
        }
        return String.format("%s ~ %s, α[%s], ρ[%s]", f, g, proximity, sb.delete(sb.length() - 1, sb.length()));
    }
    
    public static ProximityRelation parse(String relation) {
        int openBrace = relation.indexOf('{');
        int closedBrace = relation.indexOf('}');
        int openBracket = relation.indexOf('[');
        int closedBracket = relation.indexOf(']');
        
        if (!(openBrace < closedBrace && openBracket < closedBracket)) {
            throw new IllegalArgumentException(String.format("Malformed proximity relation %s", relation));
        }
        int firstOpen = Math.min(openBrace, openBracket);
        int lastClosed = Math.max(closedBrace, closedBracket);
        
        String[] heads = (relation.substring(0, firstOpen) + relation.substring(lastClosed + 1)).trim().split("\\s+");
        String proximity = relation.substring(openBracket + 1, closedBracket).trim();
        
        int[][] parsedArgRelations = new int[0][];
        
        String brace = relation.substring(openBrace + 1, closedBrace).trim();
        if (!brace.isEmpty()) {
            String[] argRelations = brace.substring(brace.indexOf('(') + 1, brace.lastIndexOf(')'))
                    .replaceAll("\\s", "")
                    .split("[(),]+");
            parsedArgRelations = new int[argRelations.length / 2][];
            for (int i = 0; i < parsedArgRelations.length; i++) {
                parsedArgRelations[i] = new int[2];
                parsedArgRelations[i][0] = Integer.parseInt(argRelations[i * 2]);
                parsedArgRelations[i][1] = Integer.parseInt(argRelations[i * 2 + 1]);
            }
        }
        
        return new ProximityRelation(heads[0], heads[1], Float.parseFloat(proximity), parsedArgRelations);
    }
}
