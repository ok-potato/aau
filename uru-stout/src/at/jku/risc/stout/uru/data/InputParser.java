/*
 * Copyright 2016 Alexander Baumgartner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.jku.risc.stout.uru.data;

import at.jku.risc.stout.uru.algo.UnificationEquation;
import at.jku.risc.stout.uru.algo.UnificationProblem;
import at.jku.risc.stout.uru.data.atom.Function;
import at.jku.risc.stout.uru.data.atom.HedgeVar;
import at.jku.risc.stout.uru.data.atom.NodeFactory;
import at.jku.risc.stout.uru.data.atom.TermVar;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * Tiny term parser with the following rules:
 * <ul>
 * <li>[A-Za-z1-9_.] are allowed for variable names and function names. Names
 * can be of any length.
 * <li>If the name is followed by an opening parenthesis a {@linkplain Function}
 * will be generated. Parameter inside the pair of parenthesis are wrapped in a
 * {@linkplain Hedge}
 * <li>Else if the first letter of the name is within [
 * {@linkplain #VARIABLE_START}, z] an {@linkplain TermVar individual variable}
 * will be generated.
 * <ul>
 * <li>If the variable name starts with an upper case letter it will become a
 * {@linkplain HedgeVar hedge variable}. Otherwise an {@linkplain TermVar
 * individual variable} will be instantiates.
 * </ul>
 * <li>Else a {@linkplain Function} with zero parameters (=constant) will be
 * generated.
 * </ul>
 *
 * @author Alexander Baumgartner
 */
public class InputParser {
    /**
     * default = 'u' => A word starting with 'u-z' will become a variable.<br>
     * {@linkplain InputParser#VARIABLE_START} has to be a lower case letter!
     * Hedge variables are denoted by upper case letters.
     */
    public static final char VARIABLE_START = 'u';
    public static final char OPENING_PARENTHESIS = '(';
    public static final char CLOSING_PARENTHESIS = ')';
    private int codePoint;
    private final UnificationProblem problem;
    private final NodeFactory nodeFactory;
    
    public InputParser(UnificationProblem problem) {
        this.problem = problem;
        this.nodeFactory = new NodeFactory();
    }
    
    /**
     * Parse the given input string and add all equations to the system. Use
     * {@linkplain #parseHedgeEquation(Reader, Reader)} if possible! It performs
     * better and saves space by reading directly from the data streams;)
     *
     * @throws IOException
     * @throws MalformedTermException
     */
    public void parseEqSystem(String problemSet, PrintStream debug) throws IOException, MalformedTermException {
        for (String equation : problemSet.split(";")) {
            String[] terms = equation.split("[^._a-zA-Z0-9()]*=[^._a-zA-Z0-9()]*");
            if (terms.length == 2) {
                // trim s and t
                String s = terms[0].replaceAll("^[^._a-zA-Z0-9()]+", "").replaceAll("[^._a-zA-Z0-9()]+$", "");
                String t = terms[1].replaceAll("^[^._a-zA-Z0-9()]+", "").replaceAll("[^._a-zA-Z0-9()]+$", "");
                if (!s.isEmpty() && !t.isEmpty()) {
                    parseHedgeEquation(new StringReader(s), new StringReader(t));
                    if (debug != null) {
                        debug.println(problem.getFirst());
                    }
                }
            }
        }
    }
    
    /**
     * A hedge equation has the form:<br>
     * (s1, s2,...) =^= (t1, t2,...)<br>
     *
     * @param leftHedge  The left sequence (s1, s2,...)
     * @param rightHedge The right sequence (t1, t2,...)
     * @throws IOException
     * @throws MalformedTermException
     */
    public void parseHedgeEquation(Reader leftHedge, Reader rightHedge) throws IOException, MalformedTermException {
        problem.add(new UnificationEquation(parseHedge(leftHedge), parseHedge(rightHedge)));
    }
    
    private TermNode parseHedge(Reader in) throws IOException, MalformedTermException {
        Hedge h = new Hedge();
        do {
            h.add(parseTerm(in));
        } while (codePoint != -1 && !isNameChar(codePoint));
        
        if (h.isEmpty()) {
            return TermNode.empty;
        }
        return new TermNode(null, h);
    }
    
    public TermNode parseTerm(Reader in) throws IOException, MalformedTermException {
        if (nextNameChar(in)) // codePoint = "first letter"
        {
            return parseIt(in);
        }
        return null;
    }
    
    private TermNode parseIt(Reader in) throws IOException, MalformedTermException {
        String name = parseTermName(in).toString();
        if (name.isEmpty()) {
            throw new MalformedTermException("Missing parenthesis at the input term");
        }
        char nameLetter1 = name.charAt(0);
        if (codePoint == OPENING_PARENTHESIS) {
            nodeFactory.pushHedge();
            while (codePoint != CLOSING_PARENTHESIS && next(in) != CLOSING_PARENTHESIS) {
                nodeFactory.addToHedge(parseIt(in));
            }
            Hedge hedge = nodeFactory.popHedge();
            next(in); // consume closing bracket
            if (Character.toLowerCase(nameLetter1) < VARIABLE_START) {
                return nodeFactory.createFunction(name, hedge);
            }
            throw new MalformedTermException("Higher order variables are not supported");
        } else if (Character.toLowerCase(nameLetter1) < VARIABLE_START) {
            return nodeFactory.createConstant(name);
        } else if (Character.isLowerCase(nameLetter1)) {
            return nodeFactory.createTermVar(name);
        } else {
            return nodeFactory.createHedgeVar(name);
        }
    }
    
    private boolean nextNameChar(Reader in) throws IOException {
        while (!isNameChar(next(in))) {
            if (codePoint == -1) {
                return false;
            }
        }
        return true;
    }
    
    private StringBuilder parseTermName(Reader in) throws IOException {
        StringBuilder name = new StringBuilder();
        for (; isNameChar(codePoint); next(in)) {
            name.appendCodePoint(codePoint);
        }
        return name;
    }
    
    private int next(Reader in) throws IOException {
        codePoint = in.read();
        return Character.isWhitespace(codePoint) ? next(in) : codePoint;
    }
    
    public boolean isNameChar(int codePoint) {
        return codePoint == '_' || codePoint == '.' || Character.isLetterOrDigit(codePoint);
    }
}
