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

package at.jku.risc.stout.urau.algo;

import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.Queue;

import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.TermNode;
import at.jku.risc.stout.urau.data.atom.Variable;
import at.jku.risc.stout.urau.util.DataStructureFactory;

/**
 * This class encapsulates the rule based system {@linkplain AntiUnifySystem}
 * and takes care of the system branching.
 * 
 * @author Alexander Baumgartner
 */
public class AntiUnify {
	private DebugLevel debugLevel;
	private EquationSystem<AntiUnifyProblem> eqOrig;
	private Queue<AntiUnifySystem> eqBranch = DataStructureFactory.$.newDeque();

	public AntiUnify(RigidityFnc rigidFnc, EquationSystem<AntiUnifyProblem> eq,
			DebugLevel debugLevel) {
		this.debugLevel = debugLevel;
		this.eqOrig = eq;
		this.eqBranch.add(new AntiUnifySystem(eqBranch, rigidFnc, eq.clone(),
				true));
	}

	/**
	 * Start computation with the specified {@linkplain PrintStream} to show the
	 * progress of the computation. If the first argument is false, then only
	 * one result will be computed.
	 */
	public long antiUnify(boolean iterateAll, PrintStream out)
			throws IllegalAlignmentException {
		AntiUnifySystem.resetCounter(); // reset branch counter
		int count = 0;
		while (!eqBranch.isEmpty()) {
			AntiUnifySystem sys = eqBranch.poll();
			sys.compute(debugLevel, out);
			if (debugLevel == DebugLevel.VERBOSE
					|| debugLevel == DebugLevel.PROGRESS) {
				out.println("-----------");
				out.println(" Result " + sys.getBranchId() + ": " + sys);
				out.println("-----------");
				out.println("  Sigma " + sys.getBranchId() + ": "
						+ sys.getSigma());
				out.println("  Store " + sys.getBranchId() + ": "
						+ sys.getStore());
				out.println();
			} else if (debugLevel != DebugLevel.SILENT) {
				out.println(" Result " + sys.getBranchId() + ": " + sys);
			}
			for (Entry<Variable, TermNode> solved : sys.getSigma().getMapping()
					.entrySet())
				callback(sys, solved.getKey());
			count++;
			if (!iterateAll)
				break;
		}
		if (debugLevel != DebugLevel.SILENT)
			out.println(count + " generalizations found");
		return count;
	}

	/**
	 * Returns the original system of equations.
	 */
	public EquationSystem<AntiUnifyProblem> getEqOrig() {
		return eqOrig;
	}

	/**
	 * This callback function will be invoked for every found generalization. By
	 * default it does nothing. For instance, one can use this callback function
	 * to collect all the results or to filter out some interesting results.<br>
	 * In combination with a matching algorithm, one can obtain the minimal
	 * complete set of generalizations by using this callback function.
	 */
	public void callback(AntiUnifySystem result, Variable generalizationVar) {
	}
}
