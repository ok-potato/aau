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
        throw new IllegalArgumentException(STR."Called getOther on ProximityRelation \{this} with function name \{f}!");
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
            sb.append(STR."(\{map[0]},\{map[1]}),");
        }
        return STR."\{f} ~ \{g}, α[\{proximity}], ρ[\{sb.delete(sb.length() - 1, sb.length())}]";
    }
    
    public static ProximityRelation parse(String relation) {
        int openBrace = relation.indexOf('{');
        int closedBrace = relation.indexOf('}');
        int openBracket = relation.indexOf('[');
        int closedBracket = relation.indexOf(']');
        
        if (!(openBrace < closedBrace && openBracket < closedBracket)) {
            throw new IllegalArgumentException(STR."Malformed proximity relation \{relation}");
        }
        int firstOpen = Math.min(openBrace, openBracket);
        int lastClosed = Math.max(closedBrace, closedBracket);
        
        String[] heads = (relation.substring(0, firstOpen) + relation.substring(lastClosed + 1)).strip().split("\\s+");
        String proximity = relation.substring(openBracket + 1, closedBracket).strip();
        
        int[][] parsedArgRelations = new int[0][];
        
        String brace = relation.substring(openBrace + 1, closedBrace).strip();
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
