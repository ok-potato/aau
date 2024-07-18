package at.jku.risc.uarau;

import at.jku.risc.uarau.data.ProximityRelation;
import at.jku.risc.uarau.data.Term;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parser {
    static Logger log = LoggerFactory.getLogger(Parser.class);
    
    public static List<Term> parseProblem(String string) {
        List<Term> terms = new ArrayList<>();
        String[] tokens = string.split("\\?=");
        if (tokens.length != 2) {
            log.error("Need two terms per equation, {} supplied", tokens.length);
        }
        terms.add(parseTerm(tokens[0]));
        log.debug("Parsed LHS: {}", terms.get(0));
        terms.add(parseTerm(tokens[1]));
        log.debug("Parsed RHS: {}", terms.get(1));
        return terms;
    }
    
    // terms look like this: 'f(g(a,b),c,d)'
    public static Term parseTerm(String string) {
        string = string.replaceAll("\\s", "");
        // f(g(a,b),c,d) -> f(  g(  a  b  )  c  d  )
        // split...    (?<=\() => if last char was '('    , => on ','    (?=\)) => if next char is ')'
        String[] tokens = string.split("(?<=\\()|,|(?=\\))");
        
        Deque<Util.TermBuilder> subTerms = new ArrayDeque<>();
        subTerms.add(new Util.TermBuilder(","));
        for (String token : tokens) {
            assert (subTerms.peek() != null);
            if (token.equals(")")) {
                Util.TermBuilder subTerm = subTerms.pop();
                if (subTerms.peek() == null) {
                    throw new IllegalArgumentException("Too many closing parentheses in term " + string);
                }
                subTerms.peek().arguments.add(subTerm.build());
                continue;
            }
            if (token.endsWith("(")) {
                subTerms.push(new Util.TermBuilder(token.substring(0, token.length() - 1)));
                continue;
            }
            assert (Arrays.stream(new String[]{"(", ",", ")"}).noneMatch(token::contains));
            subTerms.peek().arguments.add(new Term(token));
        }
        if (subTerms.size() > 1) {
            throw new IllegalArgumentException("Unclosed parentheses in term " + string);
        }
        return subTerms.pop().arguments.get(0);
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
            log.trace("Parsed PR: {}", pr);
            proximityRelations.add(pr);
        }
        log.debug("Parsed PRs: {}", proximityRelations);
        return proximityRelations;
    }
    
    public static ProximityRelation parseProximityRelation(String string) {
        String proximity;
        String argMap;
        // find proximity + argument map
        try {
            proximity = string.substring(string.lastIndexOf('['), string.indexOf(']') + 1);
            argMap = string.substring(string.lastIndexOf('{'), string.indexOf('}') + 1);
        } catch (StringIndexOutOfBoundsException e) {
            log.error("Couldn't parse proximity or argument map from {}", string);
            throw new IllegalArgumentException();
        }
        
        // find function symbols
        String[] split = string.split(Pattern.quote(proximity) + "|" + Pattern.quote(argMap));
        List<String> rest = Arrays.stream(split)
                .flatMap(s -> Arrays.stream(s.split("\\s+")))
                .filter(s -> !StringUtils.isBlank(s))
                .collect(Collectors.toList());
        if (rest.size() != 2) {
            log.error("Couldn't parse two function symbols from {}", string);
            throw new IllegalArgumentException();
        }
        
        return new ProximityRelation(rest.get(0), rest.get(1), parseProximity(proximity), parseArgRelation(argMap));
    }
    
    public static float parseProximity(String string) {
        return Float.parseFloat(string.substring(1, string.length() - 1));
    }
    
    public static List<List<Integer>> parseArgRelation(String string) {
        List<Integer> argRelations = Arrays.stream(string.substring(1, string.length() - 1).split("[(),]+"))
                .filter(s -> !StringUtils.isBlank(s))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        
        if (argRelations.size() % 2 != 0) {
            log.error("Odd number of argument relations in {{}}", string);
            throw new IllegalArgumentException();
        }
        
        int maxFrom = 0;
        for (int i = 0; i < argRelations.size() / 2; i++) {
            maxFrom = Math.max(maxFrom, argRelations.get(i * 2));
        }
        
        List<List<Integer>> parsedArgRelations = new ArrayList<>(maxFrom);
        for (int i = 0; i < maxFrom; i++) {
            parsedArgRelations.add(new ArrayList<>());
        }
        
        for (int i = 0; i < argRelations.size() / 2; i++) {
            // 1-index -> 0-index
            parsedArgRelations.get(argRelations.get(i * 2) - 1).add(argRelations.get(i * 2 + 1) - 1);
        }
        return parsedArgRelations;
    }
}
