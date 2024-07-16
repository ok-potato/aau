package at.jku.risc.uarau;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        Collection<ProximityRelation> relations = new HashSet<>();
        relations.add(new ProximityRelation("f", "g", 0.6f, new ArrayList<>()));
        Algorithm.solve(new Term("f", new Term[0]), new Term("g", new Term[0]), relations, 0.5f);
        log.info("---");
        Algorithm.solve(new Term("f", new Term[0]), new Term("g", new Term[0]), new HashSet<>(), 0.5f);
    }
}
