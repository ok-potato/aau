package at.jku.risc.uarau;

import at.jku.risc.uarau.data.ProximityRelation;
import at.jku.risc.uarau.data.Term;
import at.jku.risc.uarau.util.Pair;
import org.junit.platform.commons.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parser {
    public static Pair<Term, Term> parseProblem(String string) {
        String[] tokens = string.split("\\?=");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Need 2 sides per equation, but got " + tokens.length);
        }
        return new Pair<>(parseTerm(tokens[0]), parseTerm(tokens[1]));
    }
    
    // terms look like this: 'f(g(a,b),c,d)'
    public static Term parseTerm(String string) {
        string = string.replaceAll("\\s", "");
        // f(g(a,b),c,d) -> f(  g(  a  b  )  c  d  )
        // split...    (?<=\() => if last char was '('    , => on ','    (?=\)) => if next char is ')'
        String[] tokens = string.split("(?<=\\()|,|(?=\\))");
        
        Stack<TermBuilder> subTerms = new Stack<>();
        subTerms.push(new TermBuilder(",")); // dummyTerm
        for (String token : tokens) {
            assert !subTerms.isEmpty();
            if (token.equals(")")) {
                TermBuilder subTerm = subTerms.pop();
                if (subTerms.isEmpty()) {
                    throw new IllegalArgumentException("Too many closing parentheses in term: " + string);
                }
                subTerms.peek().arguments.add(subTerm.build());
                continue;
            }
            if (token.endsWith("(")) {
                String head = token.substring(0, token.length() - 1);
                if (StringUtils.isBlank(head)) {
                    throw new IllegalArgumentException("Missing function name in " + string);
                }
                subTerms.push(new TermBuilder(head));
                continue;
            }
            if (Arrays.stream(new String[]{"(", ",", ")"}).anyMatch(token::contains)) {
                throw new IllegalArgumentException("Bad syntax in term: " + string);
            }
            if (StringUtils.isBlank(token)) {
                throw new IllegalArgumentException("Missing argument in sub-term of: " + string);
            }
            subTerms.peek().arguments.add(new Term(token));
        }
        if (subTerms.size() > 1) {
            throw new IllegalArgumentException("Unclosed parentheses in term: " + string);
        }
        TermBuilder dummyTerm = subTerms.pop();
        if (dummyTerm.arguments.size() > 1) {
            throw new IllegalArgumentException("Multiple top level terms on one side: " + string);
        }
        return dummyTerm.arguments.get(0);
    }
    
    // relations    :=  f g {<arg-map>} [<proximity>]   multiple relations separated by ';'
    // <arg-map>    :=  (1,1), (2,3), (3,1), ...        (must include surrounding '{}')
    // <proximity>  :=  float between 0.0 and 1.0       (must include surrounding '[]')
    public static Set<ProximityRelation> parseProximityRelations(String string) {
        Set<ProximityRelation> proximityRelations = new HashSet<>();
        for (String token : string.split(";")) {
            if (StringUtils.isBlank(token)) {
                continue;
            }
            proximityRelations.add(parseProximityRelation(token));
        }
        return proximityRelations;
    }
    
    public static ProximityRelation parseProximityRelation(String string) {
        // find proximity + argument relation
        String proximity;
        String argRelation;
        try {
            proximity = string.substring(string.lastIndexOf('['), string.indexOf(']') + 1);
            argRelation = string.substring(string.lastIndexOf('{'), string.indexOf('}') + 1);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Couldn't parse proximity or argument relation from: " + string);
        }
        
        // find two function symbols
        String[] split = string.split(Pattern.quote(proximity) + "|" + Pattern.quote(argRelation));
        List<String> rest = Arrays.stream(split)
                .flatMap(s -> Arrays.stream(s.split("[,()\\s]+")))
                .filter(s -> !StringUtils.isBlank(s))
                .collect(Collectors.toList());
        if (rest.size() != 2) {
            throw new IllegalArgumentException("Couldn't parse two function symbols from: " + string);
        }
        return new ProximityRelation(rest.get(0), rest.get(1), parseProximity(proximity), parseArgumentRelation(argRelation));
    }
    
    public static float parseProximity(String string) {
        return Float.parseFloat(string.substring(1, string.length() - 1));
    }
    
    public static List<List<Integer>> parseArgumentRelation(String string) {
        List<Integer> argRelationPairs;
        try {
            argRelationPairs = Arrays.stream(string.substring(1, string.length() - 1).split("[(),\\s]+"))
                    .filter(s -> !StringUtils.isBlank(s))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Some argument relation partners couldn't be parsed as int: " + string);
        }
        
        if (argRelationPairs.size() % 2 != 0) {
            throw new IllegalArgumentException("Odd number of argument relation partners in: " + string);
        }
        
        int maxFrom = 0;
        for (int i = 0; i < argRelationPairs.size() / 2; i++) {
            maxFrom = Math.max(maxFrom, argRelationPairs.get(i * 2));
        }
        
        // [1,2, 1,3, 3,1] -> [[2,3], [], [1]]
        List<List<Integer>> argRelationsIndexed = new ArrayList<>(maxFrom);
        for (int i = 0; i < maxFrom; i++) {
            argRelationsIndexed.add(new ArrayList<>());
        }
        
        for (int i = 0; i < argRelationPairs.size() / 2; i++) {
            // don't preserve 1-indexing
            argRelationsIndexed.get(argRelationPairs.get(i * 2) - 1).add(argRelationPairs.get(i * 2 + 1) - 1);
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
