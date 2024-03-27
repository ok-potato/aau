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

package at.jku.risc.stout.urau.data;

/**
 * Interface for equations which consist of two {@linkplain TermNode}s.
 * 
 * @author Alexander Baumgartner
 */
public interface Equation extends Cloneable {
	/**
	 * Adds a {@linkplain TermNode} to the left hedge of the equation.
	 */
	public void addLeft(TermNode node);

	/**
	 * Adds a {@linkplain TermNode} to the left hedge of the equation.
	 */
	public void addRight(TermNode node);

	/**
	 * The left hedge of the equation.
	 */
	public TermNode getLeft();

	/**
	 * The right hedge of the equation.
	 */
	public TermNode getRight();

	public Equation clone();
}
