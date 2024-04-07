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
import java.util.List;
import java.util.Queue;

import at.jku.risc.stout.urau.algo.AlignmentList.Alignment;
import at.jku.risc.stout.urau.algo.AlignmentList.Alignment.AlignmentAtom;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.Hedge;
import at.jku.risc.stout.urau.data.NodeFactory;
import at.jku.risc.stout.urau.data.TermNode;
import at.jku.risc.stout.urau.data.atom.Variable;
import at.jku.risc.stout.urau.util.DataStructureFactory;

/**
 * This class represents a rule based system for rigid anti-unification of
 * unranked terms and hedges.<br>
 * The algorithm G(R) is described in the paper:<br>
 * <a href="http://www.risc.jku.at/people/tkutsia/"> Temur Kutsia</a>, <a
 * href="http://www.iiia.csic.es/~levy/"> Jordi Levy</a>, <a
 * href="http://ima.udg.edu/~villaret/"> Mateu Villaret</a>. <a
 * href="http://drops.dagstuhl.de/opus/volltexte/2011/3118/pdf/11.pdf">
 * Anti-Unification for Unranked Terms and Hedges.</a> <br>
 * In: Proc. RTA 2011. Vol. 10 of LIPIcs. Schloss Dagstuhl, 2011, 219-234.
 * 
 * @author Alexander Baumgartner
 */
public class AntiUnifySystem {
	private TermNode tmpNode = NodeFactory.newNode(null, null);
	private int branchId;
	private RigidityFnc rFnc;
	private EquationSystem<AntiUnifyProblem> problemSet;
	private List<AntiUnifyProblem> store = DataStructureFactory.$.newList();
	private Substitution sigma;
	private Queue<AntiUnifySystem> branchPointer;

	private static int BRANCH_COUNT = 0;
	public static String OUTPUT_SEPARATOR = "; ";

	/**
	 * Most likely you don't need this constructor because the algorithm is
	 * encapsulated in the class {@linkplain AntiUnify} which is much easier to
	 * use.
	 */
	public AntiUnifySystem(Queue<AntiUnifySystem> eqBranch, RigidityFnc rFnc,
			EquationSystem<AntiUnifyProblem> problemSet, boolean setUID) {
		this.branchPointer = eqBranch;
		if (setUID)
			this.branchId = ++BRANCH_COUNT;
		if (sigma == null) {
			sigma = new Substitution();
			for (int i = 0, n = problemSet.size(); i < n; i++) {
				Variable genVar = problemSet.get(i).generalizationVar;
				sigma.put(genVar, NodeFactory.newNode(genVar, null));
			}
		}
		this.problemSet = problemSet;
		this.rFnc = rFnc;
	}

	/**
	 * Computes the result silently.
	 */
	public void compute() throws IllegalAlignmentException {
		compute(DebugLevel.SILENT, null);
	}

	/**
	 * Most likely you don't need this method because the algorithm is
	 * encapsulated in the class {@linkplain AntiUnify} which is much easier to
	 * use.
	 */
	public void compute(DebugLevel debugLevel, PrintStream debugOut)
			throws IllegalAlignmentException {
		while (!problemSet.isEmpty()) {
			debug(null, debugLevel, debugOut);

			AntiUnifyProblem hedgeEq = problemSet.getLast();
			problemSet.removeLast();
			AlignmentList rAlignments = rFnc.compute(hedgeEq.getLeft()
					.getHedge().top(), hedgeEq.getRight().getHedge().top());
			if (rAlignments.isEmpty()) {
				// R-Rigid Solve for Hedges
				store.add(hedgeEq);
				debug("R-Sol", debugLevel, debugOut);
			} else {
				// R-Rigid Decomposition for Hedges
				for (int i = rAlignments.size() - 1; i > 0; i--) {
					AntiUnifySystem sysBranch = branchSystem(debugLevel,
							debugOut);
					sysBranch.rRigidDecompose(hedgeEq, rAlignments.get(i));
				}
				this.rRigidDecompose(hedgeEq, rAlignments.get(0));
				debug("R-Dec", debugLevel, debugOut);
			}
		}
		boolean changed = false;
		// R-Rigid Clean Store 2
		for (int i = store.size() - 1; i >= 0; i--) {
			AntiUnifyProblem aue = store.get(i);
			if (aue.isEmpty()) {
				substitute(aue.generalizationVar, aue.getLeft());
				store.remove(i);
				changed = true;
			}
		}
		if (changed) {
			changed = false;
			debug("R-CS2", debugLevel, debugOut);
		}

		// R-Rigid Clean Store 1
		l1: for (int i = store.size() - 1; i >= 0; i--) {
			AntiUnifyProblem aue1 = store.get(i);
			Hedge left1 = aue1.getLeft().getHedge();
			Hedge right1 = aue1.getRight().getHedge();
			for (int j = i - 1; j >= 0; j--) {
				AntiUnifyProblem aue2 = store.get(j);
				Hedge left2 = aue2.getLeft().getHedge();
				Hedge right2 = aue2.getRight().getHedge();
				if (left1.equals(left2) && right1.equals(right2)) {
					substitute(aue1.generalizationVar, aue2.generalizationVar);
					store.remove(i);
					changed = true;
					continue l1;
				}
			}
		}
		if (changed) {
			changed = false;
			debug("R-CS1", debugLevel, debugOut);
		}
		List<AntiUnifyProblem> store2 = DataStructureFactory.$.newList();
		// R-Rigid Clean Store 4
		for (int i = store.size() - 1; i >= 0; i--) {
			AntiUnifyProblem aue = store.get(i);
			Hedge left1 = aue.getLeft().getHedge();
			Hedge right1 = aue.getRight().getHedge();
			int len = left1.size();
			if (len == right1.size()) {
				Hedge h = new Hedge();
				for (int j = 0; j < len; j++) {
					Variable freshVar = NodeFactory.obtainFreshTermVar();
					store2.add(new AntiUnifyProblem(freshVar, left1.get(j),
							right1.get(j)));
					h.add(NodeFactory.newNode(freshVar));
				}
				substitute(aue.generalizationVar, new TermNode(null, h));
				store.remove(i);
				changed = true;
			}
		}
		if (changed) {
			changed = false;
			if (debugLevel == DebugLevel.PROGRESS) {
				List<AntiUnifyProblem> storeTmp = store;
				store = DataStructureFactory.$.newList(store);
				store.addAll(store2);
				debug("R-CS4", debugLevel, debugOut);
				store = storeTmp;
			}
			// R-Rigid Clean Store 3
			l1: for (int i = store2.size() - 1; i >= 0; i--) {
				AntiUnifyProblem aue1 = store2.get(i);
				TermNode left1 = aue1.getLeft();
				TermNode right1 = aue1.getRight();
				for (int j = i - 1; j >= 0; j--) {
					AntiUnifyProblem aue2 = store2.get(j);
					if (left1.equals(aue2.getLeft())
							&& right1.equals(aue2.getRight())) {
						substitute(aue1.generalizationVar,
								aue2.generalizationVar);
						store2.remove(i);
						changed = true;
						continue l1;
					}
				}
			}
			store.addAll(store2);
			if (changed) {
				debug("R-CS3", debugLevel, debugOut);
			}
		}
	}

	private void debug(String rule, DebugLevel debugLevel, PrintStream out) {
		if (debugLevel == DebugLevel.PROGRESS) {
			if (rule != null)
				out.println(rule + " ==> ");
			out.println("Problem " + branchId + ": " + problemSet);
			out.println("  Store " + branchId + ": " + store);
			out.println("  Sigma " + branchId + ": " + sigma);
			out.println();
		}
	}

	private void substitute(Variable var, Variable var2) {
		tmpNode.setAtom(var2);
		tmpNode.setHedge(null);
		sigma.composeInRange(var, tmpNode);
	}

	private void substitute(Variable var, TermNode node) {
		sigma.composeInRange(var, node);
	}

	private AntiUnifySystem branchSystem(DebugLevel debugLevel, PrintStream out) {
		AntiUnifySystem sysBranch = cloneSystem(true);
		if (debugLevel == DebugLevel.PROGRESS) {
			out.println("Problem " + branchId + " spawned child problem "
					+ sysBranch.branchId);
			out.println();
		}
		branchPointer.add(sysBranch);
		return sysBranch;
	}

	private AntiUnifySystem cloneSystem(boolean incCount) {
		AntiUnifySystem clone = new AntiUnifySystem(branchPointer, rFnc,
				problemSet.clone(), incCount);
		clone.store = DataStructureFactory.$.newList(store);
		clone.sigma = sigma.clone();
		return clone;
	}

	private void rRigidDecompose(AntiUnifyProblem hedgeEq, Alignment alignment)
			throws IllegalAlignmentException {
		Hedge leftH = hedgeEq.getLeft().getHedge();
		Hedge rightH = hedgeEq.getRight().getHedge();

		// Build up substitution hedge while computing the rest
		Hedge substiHedge = new Hedge();

		// {Y0: s(0-i1)=q(0-j1)} U {Yk: s(ik-ik1)=q(jk-jk1) | 1<=k<=n-1}
		// U {Yn: s(in-END)=q(jn-END)} U S
		int leftStart = 0, rightStart = 0;
		for (int i = 0, n = alignment.size(); i < n; i++) {
			// This loop includes Y0 and all Yk's
			AlignmentAtom atomK = alignment.get(i);
			int leftEnd = atomK.idxLeft, rightEnd = atomK.idxRight;
			composeHedge(substiHedge, leftH, rightH, leftStart, rightStart,
					leftEnd, rightEnd);

			// decompose the match
			// {Zk: sk=qk | 1<=k<=n} U A
			TermNode leftT = leftH.get(leftEnd);
			TermNode rightT = rightH.get(rightEnd);
			if (leftT.getAtom() instanceof Variable
					&& rightT.getAtom() instanceof Variable)
				throw new IllegalAlignmentException(
						"The same variable is not allowed at both sides of an anti-unification equation");
			AntiUnifyProblem Zk = new AntiUnifyProblem(leftT.getHedge(),
					rightT.getHedge());
			problemSet.add(Zk);
			TermNode substi = NodeFactory.newNode(leftT.getAtom(), new Hedge());
			substi.getHedge().add(Zk.createTermNode());
			substiHedge.add(substi);

			leftStart = leftEnd + 1;
			rightStart = rightEnd + 1;
		}
		// The last case is Yn
		composeHedge(substiHedge, leftH, rightH, leftStart, rightStart,
				leftH.size(), rightH.size());

		// sigma{X -> Y0,f1(Z1),Y1,...,Yn1,fn(Zn),Yn}
		substitute(hedgeEq.generalizationVar, new TermNode(null, substiHedge));
	}

	private void composeHedge(Hedge substiHedge, Hedge leftH, Hedge rightH,
			int leftStart, int rightStart, int leftEnd, int rightEnd) {
		// The following condition is just to avoid empty equations which would
		// be substituted by the empty hedge anyway. You can safely remove this
		// condition and the result will be the same.
		if (leftStart != leftEnd || rightStart != rightEnd) {
			AntiUnifyProblem Yk = new AntiUnifyProblem();
			for (int j = leftStart; j < leftEnd; j++)
				Yk.addLeft(leftH.get(j));
			for (int j = rightStart; j < rightEnd; j++)
				Yk.addRight(rightH.get(j));
			store.add(Yk);
			substiHedge.add(Yk.createTermNode());
		}
	}

	public int getBranchId() {
		return branchId;
	}

	public RigidityFnc getRFnc() {
		return rFnc;
	}

	public EquationSystem<AntiUnifyProblem> getProblemSet() {
		return problemSet;
	}

	public List<AntiUnifyProblem> getStore() {
		return store;
	}

	public Substitution getSigma() {
		return sigma;
	}

	@Override
	public String toString() {
		return sigma.toRanString();
	}

	public static void resetCounter() {
		BRANCH_COUNT = 0;
	}
}
