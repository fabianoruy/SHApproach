package shmapper.model;

import com.change_vision.jude.api.inf.model.IClass;

import shmapper.model.Ontology.Level;

/* Represents a Concept from a Ontology. */
public class Concept extends Notion {
	private static final long	serialVersionUID	= 3167613498197598423L;
	private Ontology			ontology;

	public Concept(Ontology ontology, IClass astahClass) {
		super(astahClass);
		this.ontology = ontology;
	}

	public Ontology getOntology() {
		return ontology;
	}

	@Override
	public boolean isBasetype() {
		return (ontology.getLevel() == Level.CORE || ontology.getLevel() == Level.FOUNDATIONAL);
	}

	@Override
	public Package getPackage() {
		return ontology;
	}

}