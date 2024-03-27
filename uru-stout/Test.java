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

import at.jku.risc.stout.uru.algo.*;
import at.jku.risc.stout.uru.data.InputParser;
import at.jku.risc.stout.uru.data.MalformedTermException;

import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        System.out.println("running tests...");
        testAll();
    }
    
    public static void testAll() {
        test("f(g(x,b), X, b) =? f(g(y,x), g(a), b, b)");
        //test("f(X,Y) =? f(f(a))");
    }
    
    public static void test(String in) {
        try {
            UnificationProblem problem = new UnificationProblem();
            new InputParser(problem).parseEqSystem(in, null);
            new UnificationAlgo(problem, 10).unify(DebugLevel.PROGRESS, System.out, System.out, true);
        } catch (IOException | MalformedTermException e) {
            throw new RuntimeException(e);
        }
    }
}
