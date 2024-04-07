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

import at.jku.risc.stout.urau.algo.AntiUnifyProblem;
import at.jku.risc.stout.urau.util.TinyList;

import java.util.ArrayList;
import java.util.List;

/**
 * A system of {@linkplain AntiUnifyProblem}s
 *
 * @author Alexander Baumgartner
 */
public class EquationSystem extends TinyList<AntiUnifyProblem> implements Cloneable {
    private List<AntiUnifyProblem> equations = new ArrayList<>();
    /**
     * The separator is used to separate the equations of this system.
     */
    public static String EQ_SEPARATOR = ";  ";
    /**
     * The output will look like "{@linkplain #EQ_PREFIX1}#i#
     * {@linkplain #EQ_PREFIX2}" where #i# is the index of an equation.
     */
    public static String EQ_PREFIX1 = "EQ";
    /**
     * If {@linkplain #EQ_PREFIX2} is null then the output of the index will
     * also be omitted.
     */
    public static String EQ_PREFIX2 = ": ";
    
    public AntiUnifyProblem newEquation() {
        return new AntiUnifyProblem();
    }
    
    @Override
    public int size() {
        return equations.size();
    }
    
    @Override
    public AntiUnifyProblem get(int i) {
        return equations.get(i);
    }
    
    @Override
    public void removeLast() {
        equations.remove(size() - 1);
    }
    
    @Override
    public EquationSystem clone() {
        try {
            EquationSystem ret = (EquationSystem) super.clone();
            ret.equations = new ArrayList<>(equations.size());
            for (int i = 0, n = equations.size(); i < n; i++) {
                ret.equations.add(equations.get(i).clone());
            }
            return ret;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void add(AntiUnifyProblem equation) {
        equations.add(equation);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, n = size(); i < n; i++) {
            if (EQ_SEPARATOR != null && i != 0) {
                sb.append(EQ_SEPARATOR);
            }
            if (EQ_PREFIX1 != null) {
                sb.append(EQ_PREFIX1);
            }
            if (EQ_PREFIX2 != null) {
                sb.append(i + 1).append(EQ_PREFIX2);
            }
            sb.append(get(i));
        }
        return sb.toString();
    }
}
