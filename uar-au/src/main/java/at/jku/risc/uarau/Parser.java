package at.jku.risc.uarau;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parser {
    static Logger log = LoggerFactory.getLogger(Parser.class);
    
    public static List<Term> parseProblem(String string) {
        List<Term> terms = new ArrayList<>();
        String[] tokens = string.split("=/?");
        if (tokens.length != 2) {
            log.error("Need two terms per equation, {} supplied", tokens.length);
        }
        for (String token : tokens) {
            terms.add(parseTerm(token));
        }
        return terms;
    }
    
    // terms look like this: 'f(g(a,b),c,d)'
    public static Term parseTerm(String string) {
        string = string.replaceAll("\\s", "");
        // f(g(a,b),c,d) -> f(  g(  a  b  )  c  d  )
        // split...    (?<=\() =>if last char was '('    , =>on ','    (?=\)) =>if next char is ')'
        String[] tokens = string.split("(?<=\\() | , | (?=\\))");
        
        Deque<TermBuilder> subTerms = new ArrayDeque<>();
        subTerms.add(new TermBuilder(","));
        for (String token : tokens) {
            assert (subTerms.peek() != null);
            if (token.equals(")")) {
                TermBuilder subTerm = subTerms.pop();
                if (subTerms.peek() == null) {
                    throw new IllegalArgumentException("Too many closing parentheses in term " + string);
                }
                subTerms.peek().arguments.add(subTerm.build());
                continue;
            }
            if (token.endsWith("(")) {
                subTerms.push(new TermBuilder(token.substring(0, token.length() - 1)));
                continue;
            }
            assert (Arrays.stream(new String[]{"(", ",", ")"}).noneMatch(token::contains));
            subTerms.peek().arguments.add(new Term(token));
        }
        if (subTerms.size() > 1) {
            throw new IllegalArgumentException("Unclosed parentheses in term " + string);
        }
        Term result = subTerms.pop().arguments.get(0);
        log.debug("Parsed term: {}", result);
        return result;
    }
    
    private static class TermBuilder {
        public String head;
        public List<Term> arguments = new ArrayList<>();
        
        public TermBuilder(String head) {
            this.head = head;
        }
        
        public Term build() {
            assert (arguments != null);
            Term t = new Term(head, arguments.toArray(new Term[0]));
            arguments = null;
            log.trace("Parsed term: {}", t);
            return t;
        }
    }
    
    // relations =>  f g {<arg-map>} [<proximity>]  separated by ';'
    // <arg-map> =>  (1,1), (2,3), (3,1), ...       (with surrounding '{}')
    // <proximity> => float between 0.0 and 1.0     (with surrounding '[]')
    public static Set<ProximityRelation> parseProximityRelations(String string) {
        Set<ProximityRelation> proximityRelations = new HashSet<>();
        for (String token : string.split(";")) {
            ProximityRelation pr = parseProximityRelation(token);
            if (proximityRelations.contains(pr)) {
                log.error("Multiple declarations of relation {} <-> {}", pr.f, pr.g);
                throw new IllegalArgumentException();
            }
            log.trace("Parsed relation: {}", pr);
            proximityRelations.add(pr);
        }
        log.debug("Parsed PRs: {}", proximityRelations);
        return proximityRelations;
    }
    
    public static ProximityRelation parseProximityRelation(String token) {
        String proximity;
        String argMap;
        // find proximity + argument map
        try {
            proximity = token.substring(token.lastIndexOf('[') + 1, token.indexOf(']'));
            argMap = token.substring(token.lastIndexOf('{') + 1, token.indexOf('}'));
        } catch (StringIndexOutOfBoundsException e) {
            log.error("Couldn't parse proximity or argument map from {}", token);
            throw new IllegalArgumentException();
        }
        
        // find function symbols
        String[] partialSplit = token.split(Pattern.quote(proximity) + " | " + Pattern.quote(argMap));
        List<String> rest = new ArrayList<>();
        for (String part : partialSplit) {
            rest.addAll(Arrays.asList(part.trim().split("\\s+")));
        }
        if (rest.size() != 2) {
            log.error("Couldn't parse two function symbols from {}", token);
            throw new IllegalArgumentException();
        }
        return new ProximityRelation(rest.get(0), rest.get(1), Float.parseFloat(proximity), parseArgRelation(argMap));
    }
    
    public static List<List<Integer>> parseArgRelation(String string) {
        List<Integer> argRelations = Arrays.stream(string.split("[(),]")).map(Integer::parseInt).collect(Collectors.toList());
        if (argRelations.size() % 2 != 0) {
            log.error("Odd number of argument relations in {{}}", string);
            throw new IllegalArgumentException();
        }
        int maxFrom = 0;
        for (int i = 0; i < argRelations.size() / 2; i++) {
            maxFrom = Math.max(maxFrom, argRelations.get(i * 2));
        }
        List<List<Integer>> parsedArgRelations = Collections.nCopies(maxFrom, new ArrayList<>());
        for (int i = 0; i < argRelations.size() / 2; i++) {
            parsedArgRelations.get(argRelations.get(i * 2)).add(argRelations.get(i * 2 + 1));
        }
        return parsedArgRelations;
    }
}
