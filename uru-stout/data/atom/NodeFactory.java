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

import at.jku.risc.stout.uru.data.Hedge;
import at.jku.risc.stout.uru.data.TermNode;

import java.util.*;

/**
 * A factory to create {@linkplain TermNode}s, {@linkplain Hedge}s and atomic
 * types ( {@linkplain TermAtom}s).
 *
 * @author Alexander Baumgartner
 */
public class NodeFactory {
    
    public static String PREFIX_HedgeVar;
    public static String PREFIX_TermVar;
    public static String PREFIX_Function;
    public static String PREFIX_FreshTermVar = "&";
    public static String SUFFIX_FreshTermVar = "";
    public static String PREFIX_FreshHedgeVar = "#";
    public static String SUFFIX_FreshHedgeVar = "";
    private static long hedgeVarCnt = 0;
    private Map<String, Function> fncCache = new HashMap<>();
    private Map<String, TermNode> constCache = new HashMap<>();
    private Map<String, TermNode> hedgeVar = new HashMap<>();
    private Map<String, TermNode> termVar = new HashMap<>();
    private Deque<Hedge> hStack = new LinkedList<>();
    
    public static HedgeVar obtainFreshHedgeVar() {
        StringBuilder name = new StringBuilder();
        if (PREFIX_FreshHedgeVar != null)
            name.append(PREFIX_FreshHedgeVar);
        name.append(++hedgeVarCnt);
        if (SUFFIX_FreshHedgeVar != null)
            name.append(SUFFIX_FreshHedgeVar);
        return new HedgeVar(name.toString());
    }
    
    public static TermNode obtainFreshHedgeNode() {
        return newNode(obtainFreshHedgeVar(), null);
    }
    
    public static TermVar obtainFreshTermVar() {
        StringBuilder name = new StringBuilder();
        if (PREFIX_FreshTermVar != null)
            name.append(PREFIX_FreshTermVar);
        name.append(++hedgeVarCnt);
        if (SUFFIX_FreshTermVar != null)
            name.append(SUFFIX_FreshTermVar);
        return new TermVar(name.toString());
    }
    
    public static TermNode obtainFreshTermVarNode() {
        return newNode(obtainFreshTermVar(), null);
    }
    
    public static void resetCounter() {
        hedgeVarCnt = 0;
    }
    
    private static TermNode newNode(TermAtom atom, Hedge hedge) {
        return new TermNode(atom, hedge);
    }
    
    public void pushHedge() {
        hStack.push(new Hedge());
    }
    
    public void addToHedge(TermNode node) {
        hStack.peek().add(node);
    }
    
    public Collection<TermNode> collectHedgeVars() {
        return hedgeVar.values();
    }
    
    public Collection<TermNode> collectTermVars() {
        return termVar.values();
    }
    
    public Hedge popHedge() {
        Hedge h = hStack.pop();
        if (h.getSequence().size() == 0)
            return Hedge.nullHedge;
        return h;
    }
    
    public Hedge createHedge(TermNode... nodes) {
        if (nodes.length == 0)
            return Hedge.nullHedge;
        return new Hedge(Arrays.asList(nodes));
    }
    
    public TermNode createHedgeVar(String name) {
        if (PREFIX_HedgeVar != null)
            name = PREFIX_HedgeVar + name;
        TermNode ret = hedgeVar.get(name);
        if (ret == null) {
            ret = new TermNode(new HedgeVar(name), null);
            hedgeVar.put(name, ret);
        }
        return ret;
    }
    
    public TermNode createTermVar(String name) {
        if (PREFIX_TermVar != null)
            name = PREFIX_TermVar + name;
        TermNode ret = termVar.get(name);
        if (ret == null) {
            ret = new TermNode(new TermVar(name), null);
            termVar.put(name, ret);
        }
        return ret;
    }
    
    public TermNode createFunction(String name, Hedge hedge) {
        if (hedge.size() == 0)
            return createConstant(name);
        if (PREFIX_Function != null)
            name = PREFIX_Function + name;
        return new TermNode(obtainFunction(name), hedge);
    }
    
    public TermNode createConstant(String name) {
        if (PREFIX_Function != null)
            name = PREFIX_Function + name;
        TermNode ret = constCache.get(name);
        if (ret == null) {
            ret = new TermNode(obtainFunction(name), null);
            constCache.put(name, ret);
        }
        return ret;
    }
    
    private Function obtainFunction(String name) {
        Function ret = fncCache.get(name);
        if (ret == null) {
            ret = new Function(name);
            fncCache.put(name, ret);
        }
        return ret;
    }
}
