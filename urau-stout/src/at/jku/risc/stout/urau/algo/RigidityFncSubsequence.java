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

import at.jku.risc.stout.urau.data.TermAtomList;
import at.jku.risc.stout.urau.data.atom.TermAtom;
import at.jku.risc.stout.urau.util.CoordList;

/**
 * Implementation for rigidity function with subsequence matching.<br>
 * Let m be the number of left term atoms and n be the number of right term
 * atoms.<br>
 * Time complexity = Exponential worst case (back tracking) but should be OK in
 * average case<br>
 * Space complexity = O(m * n)<br>
 * <br>
 * This implementation reuses all the allocated objects for performance reasons.
 * That's why the space complexity will become the quadratic maximum of all m's
 * and n's. O(max(m1, ..., mk) * max(n1, ..., nk))
 * 
 * @author Alexander Baumgartner
 */
public class RigidityFncSubsequence extends RigidityFnc {
	private int minLen;
	private int traceLenLeft = 32;
	private int traceLenRight = 32;
	private int[][] traceRoute = new int[traceLenLeft][traceLenRight];
	public static boolean DEBUG = false;

	@Override
	public RigidityFnc setMinLen(int minLen) {
		if (minLen < 0)
			throw new IllegalArgumentException("minLen cannot be < 0");
		this.minLen = minLen;
		return this;
	}

	@Override
	public AlignmentList compute(TermAtomList left, TermAtomList right) {
		int lenL = left.size(), lenR = right.size();
		int minLen = this.minLen;
		AlignmentList alignList = AlignmentList.obtainList();
		if (minLen > lenL || minLen > lenR)
			return alignList;
		// check size of recycled memory space
		if (traceLenLeft <= lenL || traceLenRight <= lenR) {
			if (traceLenLeft <= lenL)
				traceLenLeft = lenL + 1;
			if (traceLenRight <= lenR)
				traceLenRight = lenR + 1;
			traceRoute = new int[traceLenLeft][traceLenRight];
		}
		int[][] traceRoute = this.traceRoute;
		int MAX_VALUE = Integer.MAX_VALUE;
		int MIN_VALUE = Integer.MIN_VALUE;

		// trace common subsequence length and directions
		int[] prevLenI = traceRoute[0];
		for (int i = 0; i < lenL;) {
			TermAtom tLeft = left.get(i);
			i++;
			int[] currLenI = traceRoute[i];
			for (int j = 0, prevLeft = 0, currLen; j < lenR;) {
				if (tLeft.equals(right.get(j))) {
					prevLeft = (prevLenI[j] & MAX_VALUE) + 1;
					// In order to save some space,
					// we use the high bit to indicate a match
					currLen = (prevLeft | MIN_VALUE);
					j++;
				} else {
					j++;
					int prevTop = (prevLenI[j] & MAX_VALUE);
					currLen = prevLeft > prevTop ? prevLeft : prevTop;
					prevLeft = currLen;
				}
				currLenI[j] = currLen;
			}
			prevLenI = currLenI;
		}

		if ((prevLenI[lenR] & MAX_VALUE) < minLen)
			return alignList;

		if (DEBUG)
			debugMatrix(left, right, System.out);
		// create result set via recursive back tracking
		alignList.nextAlignment();
		backTrack(alignList, 0, traceRoute, left, lenL, lenR);

		return alignList;
	}

	protected void debugMatrix(TermAtomList left, TermAtomList right,
			PrintStream debugOut) {
		int lenL = left.size(), lenR = right.size();
		int MAX_VALUE = Integer.MAX_VALUE;
		int[][] traceRoute = this.traceRoute;
		debugOut.println("Length matrix: (* signals a match)");
		StringBuilder out = new StringBuilder("    ");
		for (int j = 0; j < lenR; j++)
			out.append((" " + right.get(j).getName() + "    ").substring(0, 5));
		debugOut.println(out);
		out.setLength(0);
		for (int i = 1; i <= lenL; i++) {
			out.append((left.get(i - 1).getName() + "    ").substring(0, 4));
			for (int j = 1; j <= lenR; j++) {
				if (j > 1)
					out.append(',');

				int num = (traceRoute[i][j] & MAX_VALUE);
				if (traceRoute[i][j] < 0) {
					out.append('*');
				} else {
					out.append(' ');
				}
				if (num < 100) {
					if (num < 10)
						out.append('0');
					out.append('0');
				}
				out.append(num);
			}
			debugOut.println(out);
			out.setLength(0);
		}
	}

	protected void backTrack(AlignmentList alignList, int branchNum,
			int[][] traceLen, TermAtomList left, int idxL, int idxT) {
		if (traceLen[idxL][idxT] == 0)
			return;
		CoordList exitList = CoordList.obtainList();
		findExits(traceLen, idxL, idxT, exitList);
		for (int i = exitList.size() - 1; i >= 0; i--) {
			int[] exit = exitList.get(i);
			idxL = exit[0];
			idxT = exit[1];
			backTrack(alignList, alignList.size() - 1, traceLen, left, idxL,
					idxT);
			TermAtom atom = left.get(idxL);
			for (int n = alignList.size(); branchNum < n; branchNum++)
				alignList.get(branchNum).addAtom(atom, idxL, idxT);
			if (i > 0)
				alignList.nextAlignment();
		}
		exitList.free();
	}

	private void findExits(int[][] traceLen, int idxL, int idxT,
			CoordList result) {
		int MAX_VALUE = Integer.MAX_VALUE;
		int len = (traceLen[idxL][idxT] & MAX_VALUE);
		int idxL1 = idxL - 1;
		int idxT1 = idxT - 1;
		boolean moveUp = true;
		CoordList branch = CoordList.obtainList();
		for (int i = 0; i >= 0;) {
			if (traceLen[idxL][idxT] < 0) {
				if (!result.contains(idxL1, idxT1))
					result.add(idxL1, idxT1);
			}
			int walkLeft = (traceLen[idxL1][idxT] & MAX_VALUE);
			int walkUp = MAX_VALUE;
			if (moveUp)
				walkUp &= traceLen[idxL][idxT1];
			if (walkLeft == len) {
				if (walkUp == len) {
					i++;
					branch.add(idxL, idxT1);
					moveUp = false;
				}
				idxL = idxL1;
				idxL1--;
			} else if (walkUp == len) {
				idxT = idxT1;
				idxT1--;
			} else {
				if (i > 0) {
					int[] nextBranch = branch.getLast();
					idxL = nextBranch[0];
					idxT = nextBranch[1];
					idxL1 = idxL - 1;
					idxT1 = idxT - 1;
					branch.removeLast();
					moveUp = true;
				}
				i--;
			}
		}
		branch.free();
	}
}
