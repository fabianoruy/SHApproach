package shmatcher.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents the Integrated Content Model (ICM) of an Initiative. */
public class IntegratedModel extends Package {
	private String			description;
	private boolean			structural;
	private List<Concept>	concepts;
	private List<Element>	elements;

	public IntegratedModel(String name, String def, boolean struct, IPackage astahPack) {
		super(name, def, astahPack);
		this.structural = struct;
		this.concepts = new ArrayList<Concept>();
		this.elements = new ArrayList<Element>();
	}

	public String getDescription() {
		return description;
	}

	public boolean isStructural() {
		return structural;
	}

	public List<Concept> getConcepts() {
		return concepts;
	}

	public List<Element> getElements() {
		return elements;
	}

	public void addConcept(Concept concept) {
		this.concepts.add(concept);
	}

	public void addElement(Element element) {
		this.elements.add(element);
	}

	public void setDescription(String description) {
		this.description = description;
	}

}