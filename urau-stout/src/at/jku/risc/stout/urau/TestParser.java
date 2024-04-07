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

import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.InputParser;

public class TestParser {
    public static void main(String[] args) {
        testParser("f(x,g(Y,x,b),h(y,X,X(f,g),F(f,g))) =^= x, x=X(c);");
    }
    
    public static void testParser(String in) {
        EquationSystem sys = new EquationSystem();
        try {
            new InputParser(sys).parseEquationSystem(in);
            System.out.println(sys);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
