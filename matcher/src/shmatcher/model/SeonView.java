package shmatcher.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents a View extracted the SEON Ontologies. */
public class SeonView extends Package {
	private String			scope;
	private List<Ontology>	ontologies;

	public SeonView(String name, String def, IPackage astahPack) {
		super(name, def, astahPack);
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

}