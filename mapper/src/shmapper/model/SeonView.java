package shmapper.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents a View extracted the SEON Ontologies. */
public class SeonView extends Package<Concept> {
	private static final long	serialVersionUID	= 6480365986851483463L;
	private String				scope;
	private List<Ontology>		ontologies;

	public SeonView(IPackage astahPack) {
		super(astahPack);
		this.setName("SEON View");
		this.ontologies = new ArrayList<Ontology>();
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public List<Ontology> getOntologies() {
		return this.ontologies;
	}

	public void addOntology(Ontology onto) {
		this.ontologies.add(onto);
	}

	public List<Concept> getConcepts() {
		List<Concept> concepts = new ArrayList<Concept>();
		for (Ontology onto : ontologies) {
			concepts.addAll(onto.getConcepts());
		}
		return concepts;
	}
	
	@Override
	public List<Concept> getNotions() {
		return getConcepts();
	}

	public Concept getConceptByName(String name) {
		for (Concept concept : getConcepts()) {
			if (concept.getName().equals(name)) {
				return concept;
			}
		}
		return null;
	}

}