package at.jku.risc.uarau.data;

public record ProximityRelation(String f, String g, float proximity, int[][] argumentRelation) {
    public ProximityRelation(String f, String g, float proximity, int[][] argumentRelation) {
        // -> canonical representation (via fixed ordering)
        if (f.hashCode() < g.hashCode()) {
            this.f = f.intern();
            this.g = g.intern();
            this.argumentRelation = argumentRelation;
        } else {
            this.f = g.intern();
            this.g = f.intern();
            this.argumentRelation = reverse(argumentRelation);
        }
        this.proximity = proximity;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof ProximityRelation otherPR) {
            return f == otherPR.f && g == otherPR.g;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return 31 * f.hashCode() + g.hashCode();
    }
    
    private static int[][] reverse(int[][] original) {
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
        return STR."\{f} ~ \{g} [\{proximity}] \{sb.delete(sb.length() - 1, sb.length())}";
    }
    
    public static ProximityRelation parse(String relation) {
        relation = relation.strip();
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
        
        String brace = relation.substring(openBrace + 1, closedBrace);
        String[] argRelation = new String[0];
        if (brace.contains("(")) {
            argRelation = brace.substring(brace.indexOf('(') + 1, brace.lastIndexOf(')'))
                    .replaceAll("\\s", "")
                    .split("[(),]+");
        }
        String proximity = relation.substring(openBracket + 1, closedBracket).strip();
        
        int[][] argumentRelation = new int[argRelation.length / 2][];
        for (int i = 0; i < argumentRelation.length; i++) {
            argumentRelation[i] = new int[]{Integer.parseInt(argRelation[i * 2]), Integer.parseInt(argRelation[i * 2
                    + 1])};
        }
        
        return new ProximityRelation(heads[0], heads[1], Float.parseFloat(proximity), argumentRelation);
    }
}
