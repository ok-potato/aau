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

import at.jku.risc.stout.urau.util.ControlledException;

/**
 * This exception indicates that the computed alignment is not admissible.
 * 
 * @author Alexander Baumgartner
 */
public class IllegalAlignmentException extends ControlledException {
	private static final long serialVersionUID = -2425388395961466527L;

	public IllegalAlignmentException() {
		super();
	}

	public IllegalAlignmentException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalAlignmentException(String message) {
		super(message);
	}

	public IllegalAlignmentException(Throwable cause) {
		super(cause);
	}
}
