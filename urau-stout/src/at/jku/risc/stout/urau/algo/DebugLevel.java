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

/**
 * Enumeration with 4 different levels of debugging.
 * <ul>
 * <li>{@linkplain DebugLevel#SILENT} = No debug output
 * <li>{@linkplain DebugLevel#SIMPLE} = Very low debug level which only shows
 * results at the console
 * <li>{@linkplain DebugLevel#VERBOSE} = Shows some additional informations to
 * {@linkplain DebugLevel#SIMPLE}
 * <li>{@linkplain DebugLevel#PROGRESS} = Shows very possible information
 * </ul>
 * 
 * @author Alexander Baumgartner
 */
public enum DebugLevel {
	SILENT, SIMPLE, VERBOSE, PROGRESS
}