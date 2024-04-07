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
		EquationSystem<AntiUnifyProblem> sys = new EquationSystem<>() {
            @Override
            public AntiUnifyProblem newEquation() {
                return new AntiUnifyProblem();
            }
        };
        new InputParser<>(sys).parseEquationSystem(problem, null);
		System.out.println(sys);
		System.out.println();
		AntiUnify rau = new AntiUnify(r, sys, DebugLevel.SIMPLE);
		rau.antiUnify(true, System.out);

	}
}
