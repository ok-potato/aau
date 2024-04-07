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

package at.jku.risc.stout.uru.algo;

import at.jku.risc.stout.uru.data.TermNode;
import at.jku.risc.stout.uru.data.atom.HedgeVar;
import at.jku.risc.stout.uru.data.atom.Variable;

public record UnifSystem(UnificationProblem problem, Substitution sigma) {
    
    public void apply(Variable fromVar, TermNode toTerm) {
        problem.apply(fromVar, toTerm);
        sigma.compose(fromVar, toTerm);
    }
    
    public void eliminateSeqVariable(HedgeVar seqVar) {
        apply(seqVar, TermNode.empty);
    }
    
    public UnifSystem copy() {
        return new UnifSystem(problem.copy(), sigma.copy());
    }
    
    @Override
    public String toString() {
        return "<" + problem + ", " + sigma + ">";
    }
}
