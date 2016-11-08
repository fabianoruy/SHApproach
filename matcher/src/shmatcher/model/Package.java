package shmatcher.model;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents a Package (Ontology, Standard Model or ICM). */
public abstract class Package {
	private String		name;
	private String		definition;
	private Diagram		diagram;
	private IPackage	astahPack;

	public Package(String name, String def, IPackage astahPack) {
		this.name = name;
		this.definition = def;
		this.astahPack = astahPack;
	}

	public String getName() {
		return name;
	}

	public String getDefinition() {
		return definition;
	}

	public Diagram getDiagram() {
		return diagram;
	}

	public void setDiagram(Diagram diagram) {
		this.diagram = diagram;
	}

	public IPackage getAstahPack() {
		return astahPack;
	}

	@Override
	public String toString() {
		return this.name;
	}

}