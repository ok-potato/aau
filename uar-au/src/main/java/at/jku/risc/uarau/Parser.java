package at.jku.risc.uarau;

import at.jku.risc.uarau.data.ProximityRelation;
import at.jku.risc.uarau.data.Term;
import at.jku.risc.uarau.util.Pair;
import at.jku.risc.uarau.util.DataUtil;
import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parser {
    static Logger log = LoggerFactory.getLogger(Parser.class);
    
    public static Pair<Term, Term> parseProblem(String string) {
        String[] tokens = string.split("\\?=");
        if (tokens.length != 2) {
            log.error("Need 2 terms per equation, {} supplied", tokens.length);
            throw new IllegalArgumentException();
        }
        Term lhs = parseTerm(tokens[0]);
        log.debug("Parsed LHS: {}", lhs);
        Term rhs = parseTerm(tokens[1]);
        log.debug("Parsed RHS: {}", rhs);
        return new Pair<>(lhs, rhs);
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
                    log.error("Too many closing parentheses in term \"{}\"", string);
                    throw new IllegalArgumentException();
                }
                subTerms.peek().arguments.add(subTerm.build());
                continue;
            }
            if (token.endsWith("(")) {
                String head = token.substring(0, token.length() - 1);
                if (StringUtils.isBlank(head)) {
                    log.error("Missing function name in {}", string);
                    throw new IllegalArgumentException();
                }
                subTerms.push(new TermBuilder(head));
                continue;
            }
            if (Arrays.stream(new String[]{"(", ",", ")"}).anyMatch(token::contains)) {
                log.error("Bad syntax in term {}", string);
                throw new IllegalArgumentException();
            }
            if (StringUtils.isBlank(token)) {
                log.error("Missing argument in sub-term of {}", string);
                throw new IllegalArgumentException();
            }
            subTerms.peek().arguments.add(new Term(token));
        }
        if (subTerms.size() > 1) {
            log.error("Unclosed parentheses in term \"{}\"", string);
            throw new IllegalArgumentException();
        }
        TermBuilder dummyTerm = subTerms.pop();
        if (dummyTerm.arguments.size() > 1) {
            log.error("Multiple top level terms on one side: {}", string);
            throw new IllegalArgumentException();
        }
        return dummyTerm.arguments.get(0);
    }
    
    // relations =>  f g {<arg-map>} [<proximity>]  separated by ';'
    // <arg-map> =>  (1,1), (2,3), (3,1), ...       (with surrounding '{}')
    // <proximity> => float between 0.0 and 1.0     (with surrounding '[]')
    public static Set<ProximityRelation> parseProximityRelations(String string) {
        Set<ProximityRelation> proximityRelations = new HashSet<>();
        for (String token : string.split(";")) {
            if (StringUtils.isBlank(token)) {
                continue;
            }
            ProximityRelation pr = parseProximityRelation(token);
            log.trace("Parsed PR: {}", pr);
            proximityRelations.add(pr);
        }
        log.debug("Parsed PR's: {}", DataUtil.str(proximityRelations));
        return proximityRelations;
    }
    
    public static ProximityRelation parseProximityRelation(String string) {
        String proximity;
        String argRelation;
        // find proximity + argument relation
        try {
            proximity = string.substring(string.lastIndexOf('['), string.indexOf(']') + 1);
            argRelation = string.substring(string.lastIndexOf('{'), string.indexOf('}') + 1);
        } catch (StringIndexOutOfBoundsException e) {
            log.error("Couldn't parse proximity or argument relation from \"{}\"", string);
            throw new IllegalArgumentException();
        }
        
        // find function symbols
        String[] split = string.split(Pattern.quote(proximity) + "|" + Pattern.quote(argRelation));
        List<String> rest = Arrays.stream(split)
                .flatMap(s -> Arrays.stream(s.split("\\s+")))
                .filter(s -> !StringUtils.isBlank(s))
                .collect(Collectors.toList());
        if (rest.size() != 2) {
            log.error("Couldn't parse two function symbols from \"{}\"", string);
            throw new IllegalArgumentException();
        }
        return new ProximityRelation(rest.get(0), rest.get(1), parseProximity(proximity), parseArgumentRelation(argRelation));
    }
    
    public static float parseProximity(String string) {
        return Float.parseFloat(string.substring(1, string.length() - 1));
    }
    
    public static List<List<Integer>> parseArgumentRelation(String string) {
        List<Integer> argRelations;
        try {
            argRelations = Arrays.stream(string.substring(1, string.length() - 1).split("[(),\\s]+"))
                    .filter(s -> !StringUtils.isBlank(s))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            log.error("Couldn't parse some argument relations as integers in \"{}\"", string);
            throw new IllegalArgumentException();
        }
        
        if (argRelations.size() % 2 != 0) {
            log.error("Odd number of argument relations in \"{}\"", string);
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
            log.trace("Parsed term: {}", t);
            return t;
        }
    }
}
