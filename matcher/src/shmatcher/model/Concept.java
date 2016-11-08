package shmatcher.model;

import com.change_vision.jude.api.inf.model.IClass;

/* Represents a Concept from a Ontology. */
public class Concept extends Notion {
	private Ontology ontology;

	public Concept(String name, String def, String ster, Ontology ontology, IClass astahClass) {
		super(name, def, ster, astahClass);
		this.ontology = ontology;
	}

	public Ontology getOntology() {
		return ontology;
	}

}