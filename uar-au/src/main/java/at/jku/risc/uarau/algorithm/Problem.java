package at.jku.risc.uarau.algorithm;

import at.jku.risc.uarau.data.ProximityMap;
import at.jku.risc.uarau.data.ProximityRelation;
import at.jku.risc.uarau.data.Term;

import java.util.Arrays;
import java.util.Stack;

public class Problem {
    Term lhs, rhs;
    ProximityMap R;
    float lambda;
    
    public Problem(Term lhs, Term rhs, ProximityMap r, float lambda) {
        this.lhs = lhs;
        this.rhs = rhs;
        R = r;
        this.lambda = lambda;
    }
    
    public static Problem parse(String problem, String relations, String lambda) {
        String[] problemTerms = problem.split("=\\^=");
        return new Problem(parseTerm(problemTerms[0]), parseTerm(problemTerms[1]), parseRelations(relations), Float.parseFloat(lambda));
    }
    
    // terms look like this:  'f(g(a, b), c, d)'
    // you can write function/variable names using any symbols besides '(),'
    // whitespace is ignored
    
    private static Term parseTerm(String term) {
        // 'f(g(a, b), c, d)'  =>  {'f(', 'g(', 'a', 'b', ')', 'c', 'd', ')'}
        String[] tokens = term.replaceAll("\\s", "").split("((?<=\\()|,|(?=\\)))");
        Stack<Term> terms = new Stack<>();
        // "dummy term", avoids a null pointer on the final ")"
        terms.add(Term.function(" "));
        
        for (String s : tokens) {
            try {
                if (")".equals(s)) {
                    Term subTerm = terms.pop();
                    terms.peek().arguments.add(subTerm);
                    continue;
                }
                if (s.endsWith("(")) {
                    terms.add(Term.function(s.substring(0, s.length() - 1)));
                    continue;
                }
                terms.peek().arguments.add(Term.variable(s));
            } catch (Exception e) {
                String msg = STR."Error while parsing term '\{term}' at token '\{s}', check syntax!";
                if (")".equals(s)) {
                    msg += " (Too many closing parentheses)";
                }
                throw new IllegalArgumentException(msg);
            }
        }
        if (!" ".equals(terms.peek().head)) {
            throw new IllegalArgumentException(STR."Term \{term} contains \{terms.size() - 1} unclosed parentheses!");
        }
        
        // dereference "dummy term"
        return terms.pop().arguments.getFirst();
    }
    
    // relations looks like this:  f g {<arg-map>} [<proximity>]  ;  g h {<arg-map>} [<proximity>]  ;  ...
    //     <arg-map> looks like this:  (1,1), (2,3), (3,1), ...       surrounded by {}
    //     <proximity> is a float between 0.0 and 1.0                 surrounded by []
    
    private static ProximityMap parseRelations(String map) {
        var proximityMap = new ProximityMap();
        String[] relations = map.split(";");
        for (String relation : relations) {
            proximityMap.add(parseRelation(relation));
        }
        return proximityMap;
    }
    
    private static ProximityRelation parseRelation(String relation) {
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
        String[] argRelation = brace.substring(brace.indexOf('(') + 1, brace.lastIndexOf(')')).replaceAll("\\s", "").split("[(),]+");
        String proximity = relation.substring(openBracket + 1, closedBracket).strip();
        
        int[][] argumentRelation = new int[argRelation.length / 2][];
        for (int i = 0; i < argumentRelation.length; i ++) {
            argumentRelation[i] = new int[]{Integer.parseInt(argRelation[i*2]), Integer.parseInt(argRelation[i*2+1])};
        }
        
        return new ProximityRelation(heads[0], heads[1], Float.parseFloat(proximity), argumentRelation);
    }
    
    @Override
    public String toString() {
        return STR."Problem:\n  \{lhs}  =^=  \{rhs}\n  R: \{R}\n  λ: \{lambda}";
    }
}
