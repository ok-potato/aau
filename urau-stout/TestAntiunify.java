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

import java.io.IOException;

import at.jku.risc.stout.urau.algo.AntiUnify;
import at.jku.risc.stout.urau.algo.AntiUnifyProblem;
import at.jku.risc.stout.urau.algo.DebugLevel;
import at.jku.risc.stout.urau.algo.IllegalAlignmentException;
import at.jku.risc.stout.urau.algo.RigidityFnc;
import at.jku.risc.stout.urau.algo.RigidityFncSubsequence;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.InputParser;
import at.jku.risc.stout.urau.data.MalformedTermException;

public class TestAntiunify {
	public static void main(String[] args) {
		try {
			// String l = "f(a),f(a)";
			// String r = "f(a),f";
			// String l = "f(a,b,c),g(a),h(a)";
			// String r = "f(a,b,c),g(a),h(a)";
			// String l = "f(g(a,X),a,X,b)";
			// String r = "f(g(b),b)";
			// String l = "f(g(a,a),a,X,b)";
			// String r = "f(g(b,b),g(Y),b)";
			// String l =
			// "if(geq(x1, x2), then(eq(x3, add(x4, x2)), eq(x4, add(x4, 1))), else(eq(x3, sub(x4, x1)))) ";
			// String r =
			// "if(geq(y1, y2), then(eq(y3, add(y4, y2)), eq(y5, 1), eq(y4, add(y4, 5))), else(eq(y3, sub(y4, y1))))";
			// String l =
			// "sumProd(input(type(int), n), returnType(bvoid), eq(type(float), n, 0.0), eq(type(float), prod, 1.0), for(eq(type(int), i, 1), le(i, n), pp(i), eq(sum, plus(sum, i)), eq(prod, mult(prod, i)), foo(sum, prod)))";
			// String r =
			// "sumProd(input(type(int), n), returnType(bvoid), eq(type(float), n, 0.0), eq(type(float), prod, 1.0), for(eq(type(int), i, 1), le(i, n), pp(i), eq(sum, plus(sum, mult(i, i))), eq(prod, mult(prod, mult(i, i))), foo(sum, prod)))";
			// String l = "x,f(g(a(f,a), a, a)";
			// String r = "x,f(g(b,b), b, b)";
			String l = "f(g(a, a), g(b, b), f (g(a), g(a)))";
			String r = "f(g(a, a), f(g(a), g))";
			RigidityFnc rFnc = new RigidityFncSubsequence();
			rFnc.setMinLen(1);
			new TestAntiunify().test(l + " =^= " + r, rFnc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void test(String problem, RigidityFnc r) throws IOException,
			IllegalAlignmentException, MalformedTermException {
		EquationSystem<AntiUnifyProblem> sys = new EquationSystem<AntiUnifyProblem>() {
			@Override
			public AntiUnifyProblem newEquation() {
				return new AntiUnifyProblem();
			}
		};
		new InputParser<AntiUnifyProblem>(sys).parseEquationSystem(problem, null);
		System.out.println(sys);
		System.out.println();
		AntiUnify rau = new AntiUnify(r, sys, DebugLevel.SIMPLE);
		rau.antiUnify(true, System.out);

	}
}
