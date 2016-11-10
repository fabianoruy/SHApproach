package shmapper.model;

import java.util.List;
import java.util.ArrayList;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents an Ontology extracted from SEON. */
public class Ontology extends Package {
    private Level level;
    private List<Concept> concepts;

    public static enum Level {
	FOUNDATIONAL, CORE, DOMAIN
    }

    public Ontology(Level level, IPackage astahPack) {
	super(astahPack);
	this.level = level;
	this.concepts = new ArrayList<Concept>();
    }

    public Level getLevel() {
	return level;
    }

    public List<Concept> getConcepts() {
	return this.concepts;
    }

    public void addConcept(Concept conc) {
	this.concepts.add(conc);
    }

}