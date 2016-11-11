package shmapper.model;

import com.change_vision.jude.api.inf.model.IClass;

/* Represents a Concept from a Ontology. */
public class Concept extends Notion {
    private Ontology ontology;

    public Concept(Ontology ontology, IClass astahClass) {
	super(astahClass);
	this.ontology = ontology;
    }

    public Ontology getOntology() {
	return ontology;
    }

}