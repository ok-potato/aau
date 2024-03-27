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

package at.jku.risc.stout.urau.util;

/**
 * Every exception which will be thrown by intention is a sub-class of this one.
 * By catching this exception you will get all the controlled failure cases.
 * 
 * @author Alexander Baumgartner
 */
public abstract class ControlledException extends Exception {
	private static final long serialVersionUID = -7764972962627117950L;

	public ControlledException() {
		super();
	}

	public ControlledException(String message, Throwable cause) {
		super(message, cause);
	}

	public ControlledException(String message) {
		super(message);
	}

	public ControlledException(Throwable cause) {
		super(cause);
	}
}
