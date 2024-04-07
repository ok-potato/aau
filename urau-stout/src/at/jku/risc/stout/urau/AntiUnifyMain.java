/*
 * Copyright 2012 Alexander Baumgartner
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

package at.jku.risc.stout.urau;

import at.jku.risc.stout.urau.algo.AntiUnify;
import at.jku.risc.stout.urau.algo.DebugLevel;
import at.jku.risc.stout.urau.algo.RigidityFnc;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.InputParser;

import java.util.Arrays;

public class AntiUnifyMain {
    
    static DebugLevel debugLevel = DebugLevel.SIMPLE;
    static boolean iterateAll = false;
    static boolean justifySigma = false;
    static int minLen = 1;
    static String rigidityFnc = "at.jku.risc.stout.urau.algo.RigidityFncSubsequence";
    
    public static void main(String[] args) {
        if (args.length == 0) {
            args = new String[]{"f(g(a, a), g(b, b), f (g(a), g(a))) =^= f(g(a, a), f(g(a), g))"};
        }
        if (args.length >= 1) {
            if ("-d".equals(args[0])) {
                debugLevel = DebugLevel.PROGRESS;
                main(Arrays.copyOfRange(args, 1, args.length));
            } else if ("-v".equals(args[0])) {
                if (debugLevel != DebugLevel.PROGRESS) {
                    debugLevel = DebugLevel.VERBOSE;
                }
                main(Arrays.copyOfRange(args, 1, args.length));
            } else if ("-a".equals(args[0])) {
                iterateAll = true;
                main(Arrays.copyOfRange(args, 1, args.length));
            } else if ("-h".equals(args[0])) {
                printHelp();
            } else if ("-j".equals(args[0])) {
                justifySigma = true;
                main(Arrays.copyOfRange(args, 1, args.length));
            } else if ("-f".equals(args[0])) {
                if (args.length < 2) {
                    System.out.println("Insufficient parameters!");
                    System.out.println();
                    printHelp();
                } else {
                    rigidityFnc = args[1];
                    main(Arrays.copyOfRange(args, 2, args.length));
                }
            } else if ("-m".equals(args[0])) {
                if (args.length < 2) {
                    System.out.println("Insufficient parameters!");
                    System.out.println();
                    printHelp();
                } else {
                    minLen = Integer.parseInt(args[1]);
                    main(Arrays.copyOfRange(args, 2, args.length));
                }
            } else {
                runProblem(args[0]);
            }
        } else {
            System.out.println("Insufficient parameters!");
            System.out.println();
            printHelp();
        }
    }
    
    private static void runProblem(String problem) {
        try {
            RigidityFnc rigidity = (RigidityFnc) Class.forName(rigidityFnc).getConstructor().newInstance();
            rigidity.setMinLen(minLen);
            EquationSystem sys = new EquationSystem();
            new InputParser(sys).parseEquationSystem(problem);
            System.out.println("~~~ ~~~ ~~~ ~~~ ~~~ ~~~ ~~~ ~~~ ~~~ ~~~ ~~~ ~~~ ~~~ ~~~ ~~~ ~~~");
            
            AntiUnify rau = new AntiUnify(rigidity, sys);
            rau.antiUnify(iterateAll);
            
            System.out.println();
            System.out.println("Supported by the Austrian Science Fund (FWF) " + "under the project SToUT P 24087-N18");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void printHelp() {
        System.out.println("Anti-unification problem solver for unranked " + "terms and hedges");
        System.out.println();
        System.out.println("Give a problem of the form:");
        System.out.println("\"s1 =^= t1; s2 =^= t2; ...; sn =^= tn\"");
        System.out.println("where s1,...,sn and t1,...,tn are terms or hedges");
        System.out.println("Input example: \"(f(b, g(a)), a, f(h(a, z))) =^= " + "f(b, g(a, f(h(a, x))))\"");
        System.out.println();
        System.out.println("Simple execution example: " + "java at.jku.risc.stout.urau.AntiUnifyMain -a "
                + "\"f(a,b) =^= f(a,b,c)\"");
        System.out.println("Advanced execution example with jvm parameters:");
        System.out.println(
                "java -Xmx2048m -XX:MaxPermSize=1024m -Xss128m " + "at.jku.risc.stout.urau.AntiUnifyMain -a -m 3 -f "
                        + "\"at.jku.risc.stout.urau.algo.RigidityFncSubstring\" " + "\"f(a,b) =^= f(a,b,c)\"");
        System.out.println();
        System.out.println("  -a    iterate all possibilities");
        System.out.println("  -f c  set rigidity function to c where " + "c is the full class name");
        System.out.println("  -m n  set minimum alignment length to n");
        System.out.println("  -v    verbose output mode prints sigma and the store");
        System.out.println("  -h    print this information");
        System.out.println("  -d    debug mode prints out every available information");
    }
}
