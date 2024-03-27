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

import at.jku.risc.stout.urau.algo.AlignmentList.Alignment;
import at.jku.risc.stout.urau.data.TermAtomList;
import at.jku.risc.stout.urau.data.atom.TermAtom;

/**
 * Implementation for rigidity function with substring matching.<br>
 * Let m be the number of left term atoms and n be the number of right term
 * atoms.<br>
 * Time complexity = O(m * n)<br>
 * Space complexity = O(max(m, n))<br>
 * <br>
 * This implementation reuses all the allocated objects for performance reasons.
 * That's why the space complexity will become the maximum of all m's and n's.
 * O(max(m1, ..., mk, n1, ..., nk))<br>
 * <br>
 * Possible optimization:
 * 
 * <pre>
 *     trim off the matching items at the beginning
 *     while start <= m_end and start <= n_end and X[start] = Y[start]
 *         start := start + 1
 *     trim off the matching items at the end
 *     while start <= m_end and start <= n_end and X[m_end] = Y[n_end]
 *         m_end := m_end - 1
 *         n_end := n_end - 1
 * </pre>
 * 
 * @author Alexander Baumgartner
 */
public class RigidityFncSubstring extends RigidityFnc {
	private int minLen;
	private int traceLen = 32;
	// 4 * max(m, n) ~ O(max(m, n))
	private int[][] matchTrace = new int[4][traceLen];

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
		int matchLen = minLen;
		AlignmentList alignList = AlignmentList.obtainList();
		if (matchLen > lenL || matchLen > lenR)
			return alignList;
		int matchCount = 0;
		// check size of recycled memory space
		if (traceLen < lenL || traceLen < lenR) {
			traceLen = Math.max(lenL, lenR);
			matchTrace = new int[4][traceLen];
		}
		int[] traceMaxI = matchTrace[0];
		int[] traceMaxJ = matchTrace[1];
		int[] traceCurr = matchTrace[2];
		int[] tracePrev = matchTrace[3];

		// find longest common substrings
		for (int i = 0; i < lenL; i++) {
			TermAtom tLeft = left.get(i);
			for (int j = 0; j < lenR; j++) {
				int currLen;
				if (tLeft.equals(right.get(j))) {
					if (i * j == 0) {
						currLen = 1;
					} else {
						currLen = tracePrev[j - 1] + 1;
					}
					if (currLen >= matchLen) {
						if (currLen > matchLen) {
							matchLen = currLen;
							matchCount = 0;
						}
						traceMaxI[matchCount] = i;
						traceMaxJ[matchCount] = j;
						matchCount++;
					}
				} else {
					currLen = 0;
				}
				traceCurr[j] = currLen;
			}
			int[] swap = traceCurr;
			traceCurr = tracePrev;
			tracePrev = swap;
		}

		if (matchLen == 0) {
			alignList.nextAlignment();
			return alignList;
		}

		// create result set of alignments
		for (int i = 0; i < matchCount; i++) {
			Alignment align = alignList.nextAlignment();
			int n = traceMaxI[i] + 1;
			int rIdx = traceMaxJ[i] - matchLen + 1;
			for (int lIdx = n - matchLen; lIdx < n; lIdx++, rIdx++)
				align.addAtom(left.get(lIdx), lIdx, rIdx);
		}
		return alignList;
	}
}
