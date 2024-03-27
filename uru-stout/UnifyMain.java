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

package at.jku.risc.stout.uru;

import at.jku.risc.stout.uru.algo.DebugLevel;
import at.jku.risc.stout.uru.algo.Substitution;
import at.jku.risc.stout.uru.algo.UnificationAlgo;
import at.jku.risc.stout.uru.algo.UnificationProblem;
import at.jku.risc.stout.uru.data.InputParser;
import at.jku.risc.stout.uru.data.MalformedTermException;

import java.io.IOException;
import java.util.Set;

public class UnifyMain {
    
    static DebugLevel debugLevel = DebugLevel.SIMPLE;
    static boolean justifySigma = false;
    private static int maxDepth = 1000;
    
    @SuppressWarnings("ConstantConditions")
    public static void main(String[] args) {
        // TODO remove args override
        args = new String[]{"-j", "-m", "10", "-d", "f(g(x,b), X, b) =? f(g(y,x), g(a), b, b);f(g(x,b), X, b) =? f(g(y,x), g(a), b, b)"};
        
        StringBuilder problemSet = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printHelp();
                    return;
                case "-d":
                    debugLevel = DebugLevel.PROGRESS;
                    break;
                case "-j":
                    justifySigma = true;
                    break;
                case "-m":
                    try {
                        maxDepth = Integer.parseInt(args[++i]);
                        break;
                    } catch (Exception e) {
                        System.out.println("ERROR: '-m' must be followed by the desired maximum derivation depth!");
                        printHelp();
                        return;
                    }
                default:
                    // Allow separation by whitespace or by ";", or both
                    if (problemSet.isEmpty() || problemSet.toString().endsWith(";") || args[i].startsWith(";")) {
                        problemSet.append(args[i]);
                    } else {
                        problemSet.append(";").append(args[i]);
                    }
                    break;
            }
        }
        if (problemSet.isEmpty()) {
            System.out.println("ERROR: no problem(s) provided!");
            return;
        }
        try {
            var problem = new UnificationProblem();
            new InputParser(problem).parseEqSystem(problemSet.toString(), null);
            var algo = new UnificationAlgo(problem, maxDepth);
            Set<Substitution> substitutions = algo.unify(debugLevel, null, null, justifySigma);
            System.out.println(substitutions);
            
            System.out.println();
            System.out.println("Supported by the Austrian Science Fund (FWF) " + "under the project SToUT P 24087-N18");
        } catch (IOException | MalformedTermException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void printHelp() {
        System.out.println("Sequence unification problem solver for unranked terms");
        System.out.println();
        System.out.println("Give a problem of the form:");
        System.out.println("\"s1 =? t1; s2 =? t2; ...; sn =? tn\"");
        System.out.println("where s1,...,sn and t1,...,tn are terms or hedges");
        System.out.println("Input example: \"f(X,Y) =? f(f(a))\"");
        System.out.println();
        System.out.println("Simple execution example: java at.jku.risc.stout.uru.UnifyMain -a \"f(a,X) =? f(a,b,c,d)\"");
        System.out.println("Advanced execution example with jvm parameters:");
        System.out.println("java -Xmx2048m -XX:MaxPermSize=1024m -Xss128m at.jku.risc.stout.uru.UnifyMain -j -m 10 -d \"f(a,X) =? f(a,b,c,d)\"");
        System.out.println();
        System.out.println("  -j    justify computed unifiers");
        System.out.println("  -m n  set maximum derivation depth to n");
        System.out.println("  -h    print this information");
        System.out.println("  -d    debug mode prints out every available information");
    }
}
