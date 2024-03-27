/*
 * Copyright 2016 Alexander Baumgartner
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

package at.jku.risc.stout.uru.data.atom;

import at.jku.risc.stout.uru.data.TermNode;
import at.jku.risc.stout.uru.util.Printable;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * Base class for all the atomic types like function symbols and variables.
 *
 * @author Alexander Baumgartner
 */
public abstract class TermAtom extends Printable {
    public static final TermAtom nullAtom = new TermAtom("") {
    };
    private final String name;
    
    TermAtom(String name) {
        this.name = name.intern();
    }
    
    public String getName() {
        return name;
    }
    
    public TermAtom copy() {
        return this;
    }
    
    @Override
    public void print(Writer out) throws IOException {
        out.append(getName());
    }
    
    public TermNode substitute(Variable from, TermNode to, TermNode thisNode) {
        return thisNode;
    }
    
    public TermNode apply(Map<Variable, TermNode> sigma, TermNode thisNode) {
        return thisNode;
    }
}
