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
import java.io.StringReader;

import at.jku.risc.stout.urau.algo.AlignmentList;
import at.jku.risc.stout.urau.algo.AntiUnifyProblem;
import at.jku.risc.stout.urau.algo.RigidityFnc;
import at.jku.risc.stout.urau.algo.RigidityFncSubsequence;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.InputParser;
import at.jku.risc.stout.urau.data.MalformedTermException;
import at.jku.risc.stout.urau.data.TermAtomList;

public class TestRigid {
	public static void main(String[] args) {
		try {
			// f(a), f(b), f(c)
			// String l = "f,a,  f,b,  f,c";
			// f(f(a), g(c))
			// String l = "f,f,a,  g,c";
			// f(f(c))
			// String r = "f,f,c";
			// h(a, h(b, b), d)
			// String l = "h,a, h,b, b,  d";
			// h(a, b), b, d
			// String r = "h,a, b,  b, d";
			// String l = "f,X1,h,a,b,X2,e,a,d,e";
			// String r = "f,g,h,b,c,g,d,a";
			// String l =
			// "(d,s,d,f,g,l,k,j,h,l,h,b,g,a,l,k,j,f,h,s,d,g,s,k,d,g,j,h,s,g,b,h)";
			// String r =
			// "(s,k,d,s,d,l, k,j,d,f,g,h,s,l,d,h,b,g,s,l,k,d,j,f,h,g,d,g,k,d,s,k,s)";
			// String l =
			// "(symbol1,symbol2,symbol1,symbol1,symbol3,symbol1,symbol2,symbol1)";
			// String r =
			// "(symbol3,symbol2,symbol2,symbol2,symbol2,symbol1,symbol3,symbol2,symbol1,symbol1)";
			// RigidityFncSubsequence.DEBUG = true;
			String l = "d,s,d,f,g,l,k,j,h,l,h,b,g,a,l,k,j,f,h,s,d,g,s,k,d,g,j,h,s,g,b,h";
			String r = "s,k,d,s,d,l, k,j,d,f,g,h,s,l,d,h,b,g,s,l,k,d,j,f,h,g,d,g,k,d,s,k,s";
			RigidityFnc rFnc = new RigidityFncSubsequence();
			rFnc.setMinLen(3);
			new TestRigid().test(l, r, rFnc, 5, 100000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void test(String hedgeL, String hedgeR, RigidityFnc r, int timesI,
			int timesJ) throws IOException, MalformedTermException {
		EquationSystem<AntiUnifyProblem> sys = new EquationSystem<AntiUnifyProblem>() {
			@Override
			public AntiUnifyProblem newEquation() {
				return new AntiUnifyProblem();
			}
		};
		new InputParser<AntiUnifyProblem>(sys).parseHedgeEquation(
				new StringReader(hedgeL), new StringReader(hedgeR));

		TermAtomList topLeft = sys.getLast().getLeft().getHedge().top();
		TermAtomList topRight = sys.getLast().getRight().getHedge().top();
		AlignmentList alignment = AlignmentList.obtainList();

		for (int i = 0; i < timesI; i++) {
			long time1 = System.nanoTime();
			for (int j = timesJ; j > 0; j--) {
				alignment.free();
				alignment = r.compute(topLeft, topRight);
			}
			long time2 = System.nanoTime();
			// System.out.println(alignment);
			System.out.println("size: " + alignment.size() + " time: "
					+ (time2 - time1) / 1000000000f);
		}
	}
}
