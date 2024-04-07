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

import at.jku.risc.stout.urau.data.Equation;
import at.jku.risc.stout.urau.data.Hedge;
import at.jku.risc.stout.urau.data.NodeFactory;
import at.jku.risc.stout.urau.data.TermNode;
import at.jku.risc.stout.urau.data.atom.Variable;

/**
 * This class represents an anti-unification problem (AUP) which consists of one
 * generalization variable (the most general generalization), and two
 * {@linkplain Hedge}s. The hedges represent the left hand side and the right
 * hand side of the equation.
 * 
 * @author Alexander Baumgartner
 */
public class AntiUnifyProblem implements Equation, Cloneable {
	public Variable generalizationVar = NodeFactory.obtainFreshHedgeVar();
	private TermNode left, right;
	public static String PRINT_EQ_SEPARATOR = " =^= ";

	/**
	 * Create an AUP with the given generalization variable and hedges.
	 */
	public AntiUnifyProblem(Variable generalizationVar, TermNode left,
			TermNode right) {
		this.generalizationVar = generalizationVar;
		this.left = left;
		this.right = right;
	}

	/**
	 * Create an AUP with a fresh hedge variable as generalization variable and
	 * the given hedges.
	 */
	public AntiUnifyProblem(Hedge left, Hedge right) {
		generalizationVar = NodeFactory.obtainFreshHedgeVar();
		this.left = NodeFactory.newNode(null, left);
		this.right = NodeFactory.newNode(null, right);
	}

	/**
	 * Obtain an empty AUP with a fresh hedge variable as generalization
	 * variable.
	 */
	public AntiUnifyProblem() {
		this(new Hedge(), new Hedge());
	}

	/**
	 * Create a {@linkplain TermNode} of the generalization variable.
	 */
	public TermNode createTermNode() {
		return NodeFactory.newNode(generalizationVar);
	}

	@Override
	public void addLeft(TermNode left) {
		this.left.getHedge().add(left);
	}

	@Override
	public void addRight(TermNode right) {
		this.right.getHedge().add(right);
	}

	/**
	 * Returns the left node of this AUP.
	 */
	public TermNode getLeft() {
		return left;
	}

	/**
	 * Returns the right node of this AUP.
	 */
	public TermNode getRight() {
		return right;
	}

	public boolean isEmpty() {
		return left.isNullAtom() && right.isNullAtom()
				&& left.getHedge().size() == 0 && right.getHedge().size() == 0;
	}

	@Override
	public AntiUnifyProblem clone() {
		try {
			AntiUnifyProblem clone = (AntiUnifyProblem) super.clone();
			clone.left = left.clone();
			clone.right = right.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return generalizationVar + ": " + left.toString() + PRINT_EQ_SEPARATOR
				+ right.toString();
	}
}
