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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Base class which demands a function to print an object representation into an
 * arbitrary {@linkplain Writer}. The method {@linkplain #toString()} will also
 * be redirect to this printing function by default.
 * 
 * @author Alexander Baumgartner
 */
public abstract class Printable {

	@Override
	public String toString() {
		StringWriter sw = new StringWriter(1024);
		try {
			print(sw);
		} catch (IOException e) { // should never happen
			e.printStackTrace(new PrintWriter(sw));
		}
		return sw.toString();
	}

	public abstract void print(Writer out) throws IOException;
}
