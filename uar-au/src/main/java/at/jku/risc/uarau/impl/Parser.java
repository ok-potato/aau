package at.jku.risc.uarau.impl;

import at.jku.risc.uarau.Problem;
import at.jku.risc.uarau.ProximityRelation;
import at.jku.risc.uarau.term.GroundTerm;
import at.jku.risc.uarau.term.MappedVariableTerm;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Pair;
import at.jku.risc.uarau.util.Panic;
import at.jku.risc.uarau.util.Data;
import org.junit.platform.commons.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * {@linkplain Parser} can parse {@linkplain Parser#parseEquation(String) equations}
 * (or individual {@linkplain Parser#parseTerm(String) terms})
 * and {@linkplain Parser#parseProximityRelation(String) proximity relations} represented as strings.
 * <br><br>
 * This gets called by the string-based {@linkplain Problem#Problem(String) Problem constructors} and
 * {@linkplain Problem#proximityRelations(String)}.
 */
public class Parser {
    /**
     * Example input: {@code "f(g(x1, y), a()) ?= g(b(), x2)"}
     * <br>
     * See {@linkplain Problem#Problem(String) Problem(String equation)} for details
     */
    public static Pair<GroundTerm, GroundTerm> parseEquation(String equationString) {
        String[] tokens = equationString.split("\\?=");
        if (tokens.length != 2) {
            throw Panic.parse("Need 2 sides per equation, but got %s", tokens.length);
        }
        return new Pair<>(parseTerm(tokens[0]), parseTerm(tokens[1]));
    }
    
    /**
     * Example input: {@code "f(g(x, y), a())"}
     * <br>
     * See {@linkplain Problem#Problem(String) Problem(String equation)} for details.
     */
    public static GroundTerm parseTerm(String termString) {
        termString = termString.replaceAll("\\s", "");
        // split "f(g(a,b),c,d)" -> ["f(", "g(", "a", "b", ")", "c", "d", ")"]
        // (?<=\() => if last char was '('
        //       , => if this char is ','
        //  (?=\)) => if next char is ')'
        String[] tokens = termString.split("(?<=\\()|,|(?=\\))");
        
        Stack<GroundTermBuilder> subTerms = new Stack<>();
        subTerms.push(GroundTermBuilder.dummy());
        for (String token : tokens) {
            assert !subTerms.isEmpty();
            if (token.equals(")")) {
                GroundTermBuilder subTerm = subTerms.pop();
                if (subTerms.isEmpty()) {
                    throw Panic.parse("Too many closing parentheses in term: %s", termString);
                }
                subTerms.peek().arguments.add(subTerm.build());
                continue;
            }
            if (token.endsWith("(")) {
                String head = token.substring(0, token.length() - 1);
                if (StringUtils.isBlank(head)) {
                    throw Panic.parse("Missing function name in term: %s", termString);
                }
                subTerms.push(new GroundTermBuilder(head));
                continue;
            }
            subTerms.peek().arguments.add(new MappedVariableTerm(token));
        }
        if (subTerms.size() > 1) {
            throw Panic.parse("Unclosed parentheses in term: %s", termString);
        }
        
        GroundTermBuilder dummyTerm = subTerms.pop();
        if (dummyTerm.arguments.size() > 1) {
            throw Panic.parse("More than one top level term on one side: %s", termString);
        }
        return dummyTerm.arguments.get(0);
    }
    
    /**
     * Example input: {@code "f g [0.5] {(1 2) (2 1)} ; a b [0.8] {}"}
     * <br>
     * See {@linkplain Problem#proximityRelations(String)} for details.
     */
    public static Set<ProximityRelation> parseProximityRelations(String relationsString) {
        Set<ProximityRelation> proximityRelations = new HashSet<>();
        for (String relation : relationsString.split(";")) {
            if (!StringUtils.isBlank(relation)) {
                proximityRelations.add(parseProximityRelation(relation));
            }
        }
        return proximityRelations;
    }
    
    /**
     * Example input: {@code "f g [0.5] {(1 2) (2 1)}"}
     * <br>
     * See {@linkplain Problem#proximityRelations(String)} for details.
     */
    public static ProximityRelation parseProximityRelation(String relationString) {
        // find proximity + argument relation
        String proximity;
        String argRelation;
        try {
            proximity = relationString.substring(relationString.lastIndexOf('['), relationString.indexOf(']') + 1);
            argRelation = relationString.substring(relationString.lastIndexOf('{'), relationString.indexOf('}') + 1);
        } catch (StringIndexOutOfBoundsException e) {
            throw Panic.parse("Couldn't get proximity or argument relation from: %s", relationString);
        }
        
        // find two function symbols
        String[] split = relationString.split(Pattern.quote(proximity) + "|" + Pattern.quote(argRelation));
        List<String> rest = Arrays.stream(split)
                .flatMap(s -> Arrays.stream(s.split("[,()\\s]+")))
                .filter(s -> !StringUtils.isBlank(s))
                .collect(Collectors.toList());
        if (rest.size() != 2) {
            throw Panic.parse("Couldn't get two function symbols from: %s", relationString);
        }
        
        float parsedProximity;
        try {
            parsedProximity = parseProximity(proximity);
        } catch (NumberFormatException e) {
            throw Panic.parse("Could not parse proximity for proximity relation: ", relationString);
        }
        
        return new ProximityRelation(rest.get(0), rest.get(1), parsedProximity, parseArgumentRelation(argRelation));
    }
    
    // [<proximity>]    :=  float between 0.0 and 1.0
    private static float parseProximity(String proximityString) {
        return Float.parseFloat(proximityString.substring(1, proximityString.length() - 1));
    }
    
    // {<arg-relation>} :=  (1,1), (2,3), (3,1), ...
    private static List<Set<Integer>> parseArgumentRelation(String argRelationString) {
        List<Integer> pairs;
        try {
            pairs = Arrays.stream(argRelationString.substring(1, argRelationString.length() - 1).split("[(),\\s]+"))
                    .filter(s -> !StringUtils.isBlank(s))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw Panic.parse("Some argument positions couldn't be parsed as int: %s", argRelationString);
        }
        
        if (pairs.size() % 2 != 0) {
            throw Panic.parse("Odd number of argument positions in: %s", argRelationString);
        }
        
        // convert to index-based representation, e.g. [1,2, 1,3, 3,1] -> [[2,3], [], [1]]
        int maxFromPosition = 0;
        for (int idx = 0; idx < pairs.size() / 2; idx++) {
            maxFromPosition = Math.max(maxFromPosition, pairs.get(idx * 2));
        }
        List<List<Integer>> argRelationsIndexed = new ArrayList<>(maxFromPosition);
        for (int idx = 0; idx < maxFromPosition; idx++) {
            argRelationsIndexed.add(new ArrayList<>());
        }
        for (int idx = 0; idx < pairs.size() / 2; idx++) {
            argRelationsIndexed.get(pairs.get(idx * 2) - 1).add(pairs.get(idx * 2 + 1) - 1);
        }
        return Data.mapList(argRelationsIndexed, ArraySet::new);
    }
    
    private static class GroundTermBuilder {
        public String head;
        public List<GroundTerm> arguments = new ArrayList<>();
        
        GroundTermBuilder(String head) {
            if (StringUtils.isBlank(head) || head.contains(",") || head.contains("(") || head.contains(")")) {
                throw Panic.state("Came up with head '%s' while parsing the problem equation", head);
            }
            this.head = head;
        }
        
        private GroundTermBuilder() {
            // called with 'dummy()'
        }
        
        static GroundTermBuilder dummy() {
            return new GroundTermBuilder();
        }
        
        public GroundTerm build() {
            GroundTerm t = new GroundTerm(head, arguments);
            arguments = null;
            return t;
        }
    }
}
