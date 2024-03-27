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

package at.jku.risc.stout.uru.data;

import at.jku.risc.stout.uru.data.atom.Function;
import at.jku.risc.stout.uru.data.atom.TermAtom;
import at.jku.risc.stout.uru.data.atom.Variable;
import at.jku.risc.stout.uru.util.Printable;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@linkplain Hedge} is a sequence of {@linkplain TermNode}s.
 *
 * @author Alexander Baumgartner
 */
public class Hedge extends Printable {
    
    public static final Hedge nullHedge = new Hedge() {
        @Override
        public void add(TermNode t) {
            throw new IllegalAccessError("Illegal null-hedge modification");
        }
        
        @Override
        public Hedge copy() {
            return this;
        }
    };
    
    public static String PRINT_PARAM_START = "(";
    public static String PRINT_PARAM_SEPARATOR = ", ";
    public static String PRINT_PARAM_END = ")";
    
    private final List<TermNode> sequence;
    
    public Hedge() {
        sequence = new ArrayList<>();
    }
    
    public Hedge(List<TermNode> sequence) {
        this.sequence = sequence;
    }
    
    public boolean isEmpty() {
        return sequence.isEmpty();
    }
    
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Hedge otherHedge)) {
            return false;
        }
        if (this.size() != otherHedge.size()) {
            return false;
        }
        for (int i = this.size() - 1; i >= 0; i--) {
            if (!this.sequence.get(i).equals(otherHedge.sequence.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return sequence.hashCode();
    }
    
    public void add(TermNode t) {
        if (t == null) {
            return;
        }
        if (t.isNullAtom()) {
            sequence.addAll(t.getHedge().sequence);
        }
        sequence.add(t);
    }
    
    public TermNode get(int i) {
        return sequence.get(i);
    }
    
    public int size() {
        return sequence.size();
    }
    
    public Hedge copy() {
        List<TermNode> cSeq = new ArrayList<>(sequence.size());
        Hedge clone = new Hedge(cSeq);
        for (TermNode termNode : sequence) {
            cSeq.add(termNode.copy());
        }
        return clone;
    }
    
    public List<TermNode> getSequence() {
        return sequence;
    }
    
    @Override
    public void print(Writer out) throws IOException {
        print(out, true);
    }
    
    public void print(Writer out, boolean isBlock) throws IOException {
        if (!sequence.isEmpty()) {
            if (isBlock) {
                out.append(PRINT_PARAM_START);
            }
            for (int i = 0, n = sequence.size(); i < n; i++) {
                if (i > 0) {
                    out.append(PRINT_PARAM_SEPARATOR);
                }
                out.append(sequence.get(i).toString());
            }
            if (isBlock) {
                out.append(PRINT_PARAM_END);
            }
        }
    }
    
    /**
     * Replaces the {@linkplain TermNode} at the given position with a new
     * {@linkplain TermNode} if the {@linkplain TermAtom} of the new node is not
     * null. Otherwise, the old {@linkplain TermNode} will be replaced by the
     * given sequence.
     *
     * @return Returns the amount of nodes inserted instead of the old one.
     */
    public int replace(int idx, TermNode toTermOrHedge) {
        if (toTermOrHedge.isNullAtom()) {
            List<TermNode> oSequence = toTermOrHedge.getHedge().sequence;
            int size = oSequence.size();
            switch (size) {
                case 0:
                    sequence.remove(idx);
                    return 0;
                case 1:
                    sequence.set(idx, oSequence.get(0));
                    return 1;
                default:
                    sequence.set(idx, oSequence.get(0));
                    sequence.addAll(idx + 1, oSequence.subList(1, size));
                    return size;
            }
        } else {
            sequence.set(idx, toTermOrHedge);
            return 1;
        }
    }
    
    public Hedge subHedge(int fromIdx, int toIdx) {
        if (toIdx <= fromIdx) {
            return nullHedge;
        }
        List<TermNode> cSeq = new ArrayList<>(toIdx - fromIdx);
        Hedge subHedge = new Hedge(cSeq);
        while (fromIdx < toIdx) {
            cSeq.add(sequence.get(fromIdx++).copy());
        }
        return subHedge;
    }
    
    public Map<TermAtom, Integer> headVars() {
        Map<TermAtom, Integer> heads = new HashMap<>();
        for (TermNode t : sequence) {
            if (t.getAtom() instanceof Variable) {
                Integer cnt = heads.get(t.getAtom());
                if (cnt == null) {
                    cnt = 0;
                }
                heads.put(t.getAtom(), cnt + 1);
            }
        }
        return heads;
    }
    
    public Map<TermAtom, Integer> headFunctions() {
        Map<TermAtom, Integer> heads = new HashMap<>();
        for (TermNode t : sequence) {
            if (t.getAtom() instanceof Function) {
                Integer cnt = heads.get(t.getAtom());
                if (cnt == null) {
                    cnt = 0;
                }
                heads.put(t.getAtom(), cnt + 1);
            }
        }
        return heads;
    }
}
