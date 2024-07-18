package at.jku.risc.uarau;

import at.jku.risc.uarau.data.ProximityRelation;
import at.jku.risc.uarau.data.Term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        Collection<ProximityRelation> relations = new HashSet<>();
        relations.add(new ProximityRelation("f", "g", 0.6f, new ArrayList<>()));
        Algorithm.solve(new Term("f", new Term[0]), new Term("g", new Term[0]), relations, 0.5f);
        Algorithm.solve(new Term("f", new Term[0]), new Term("g", new Term[0]), new HashSet<>(), 0.5f);
    }
}
