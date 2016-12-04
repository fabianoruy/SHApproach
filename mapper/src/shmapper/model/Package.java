package shmapper.model;

import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents an abstract Package (Ontology, Standard Model or ICM). */
public abstract class Package<T> extends SerializableObject {
	private static final long	serialVersionUID	= -8983523819554241974L;
	private String				name;
	private String				definition;
	private Diagram				diagram;

	public Package(IPackage astahPack) {
		this.setId(astahPack.getId());
		this.name = astahPack.getName();
		this.definition = astahPack.getDefinition();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDefinition() {
		return definition;
	}

	public abstract List<T> getNotions();

	public Diagram getDiagram() {
		return diagram;
	}

	public void setDiagram(Diagram diagram) {
		this.diagram = diagram;
	}

	@Override
	public String toString() {
		return this.name;
	}

}