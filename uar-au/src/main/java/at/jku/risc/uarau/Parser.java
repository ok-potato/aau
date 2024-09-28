package at.jku.risc.uarau;

import at.jku.risc.uarau.data.ProximityRelation;
import at.jku.risc.uarau.data.Term;
import at.jku.risc.uarau.util.Pair;
import org.junit.platform.commons.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parser {
    /**
     * @param equationStr
     * @return
     */
    public static Pair<Term, Term> parseEquation(String equationStr) {
        String[] tokens = equationStr.split("\\?=");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Need 2 sides per equation, but got " + tokens.length);
        }
        return new Pair<>(parseTerm(tokens[0]), parseTerm(tokens[1]));
    }
    
    // e.g. "f(g(a,b),c,d)"
    public static Term parseTerm(String termStr) {
        termStr = termStr.replaceAll("\\s", "");
        // split "f(g(a,b),c,d)" -> ["f(", "g(", "a", "b", ")", "c", "d", ")"]
        // (?<=\() => if last char was '('
        //       , => if this char is ','
        //  (?=\)) => if next char is ')'
        String[] tokens = termStr.split("(?<=\\()|,|(?=\\))");
        
        Stack<TermBuilder> subTerms = new Stack<>();
        subTerms.push(new TermBuilder(",")); // dummyTerm
        for (String token : tokens) {
            assert !subTerms.isEmpty();
            if (token.equals(")")) {
                TermBuilder subTerm = subTerms.pop();
                if (subTerms.isEmpty()) {
                    throw new IllegalArgumentException("Too many closing parentheses in term: " + termStr);
                }
                subTerms.peek().arguments.add(subTerm.build());
                continue;
            }
            if (token.endsWith("(")) {
                String head = token.substring(0, token.length() - 1);
                if (StringUtils.isBlank(head)) {
                    throw new IllegalArgumentException("Missing function name in " + termStr);
                }
                subTerms.push(new TermBuilder(head));
                continue;
            }
            subTerms.peek().arguments.add(new Term(token));
        }
        if (subTerms.size() > 1) {
            throw new IllegalArgumentException("Unclosed parentheses in term: " + termStr);
        }
        
        TermBuilder dummyTerm = subTerms.pop();
        if (dummyTerm.arguments.size() > 1) {
            throw new IllegalArgumentException("More than one top level term on one side: " + termStr);
        }
        return dummyTerm.arguments.get(0);
    }
    
    // relations        :=  f g {<arg-relation>} [<proximity>]   multiple relations separated by ';'
    // <arg-relation>   :=  (1,1), (2,3), (3,1), ...        (surrounded with '{}')
    // <proximity>      :=  float between 0.0 and 1.0       (surrounded with '[]')
    public static Set<ProximityRelation> parseProximityRelations(String proximityRelationsStr) {
        Set<ProximityRelation> proximityRelations = new HashSet<>();
        for (String relation : proximityRelationsStr.split(";")) {
            if (!StringUtils.isBlank(relation)) {
                proximityRelations.add(parseProximityRelation(relation));
            }
        }
        return proximityRelations;
    }
    
    public static ProximityRelation parseProximityRelation(String relationStr) {
        // find proximity + argument relation
        String proximity;
        String argRelation;
        try {
            proximity = relationStr.substring(relationStr.lastIndexOf('['), relationStr.indexOf(']') + 1);
            argRelation = relationStr.substring(relationStr.lastIndexOf('{'), relationStr.indexOf('}') + 1);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Couldn't get proximity or argument relation from: " + relationStr);
        }
        
        // find two function symbols
        String[] split = relationStr.split(Pattern.quote(proximity) + "|" + Pattern.quote(argRelation));
        List<String> rest = Arrays.stream(split)
                .flatMap(s -> Arrays.stream(s.split("[,()\\s]+")))
                .filter(s -> !StringUtils.isBlank(s))
                .collect(Collectors.toList());
        if (rest.size() != 2) {
            throw new IllegalArgumentException("Couldn't get two function symbols from: " + relationStr);
        }
        float parsedProximity = parseProximity(proximity);
        if (parsedProximity < 0.0f || parsedProximity > 1.0f) {
            throw new IllegalArgumentException("Proximity outside of range [0,1]: " + relationStr);
        }
        return new ProximityRelation(rest.get(0), rest.get(1), parsedProximity, parseArgumentRelation(argRelation));
    }
    
    // [<proximity>]    :=  float between 0.0 and 1.0
    public static float parseProximity(String proximityStr) {
        return Float.parseFloat(proximityStr.substring(1, proximityStr.length() - 1));
    }
    
    // {<arg-relation>} :=  (1,1), (2,3), (3,1), ...
    public static List<List<Integer>> parseArgumentRelation(String argRelationStr) {
        List<Integer> pairs;
        try {
            pairs = Arrays.stream(argRelationStr.substring(1, argRelationStr.length() - 1).split("[(),\\s]+"))
                    .filter(s -> !StringUtils.isBlank(s))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Some argument positions couldn't be parsed as int: " + argRelationStr);
        }
        
        if (pairs.size() % 2 != 0) {
            throw new IllegalArgumentException("Odd number of argument positions in: " + argRelationStr);
        }
        
        // convert to index-based representation, e.g. [1,2, 1,3, 3,1] -> [[2,3], [], [1]]
        int maxFromPosition = 0;
        for (int i = 0; i < pairs.size() / 2; i++) {
            maxFromPosition = Math.max(maxFromPosition, pairs.get(i * 2));
        }
        List<List<Integer>> argRelationsIndexed = new ArrayList<>(maxFromPosition);
        for (int i = 0; i < maxFromPosition; i++) {
            argRelationsIndexed.add(new ArrayList<>());
        }
        for (int i = 0; i < pairs.size() / 2; i++) {
            argRelationsIndexed.get(pairs.get(i * 2) - 1).add(pairs.get(i * 2 + 1) - 1);
        }
        return argRelationsIndexed;
    }
    
    private static class TermBuilder {
        public String head;
        public List<Term> arguments = new ArrayList<>();
        
        public TermBuilder(String head) {
            this.head = head;
        }
        
        public Term build() {
            assert arguments != null;
            Term t = new Term(head, arguments);
            arguments = null;
            return t;
        }
    }
}
