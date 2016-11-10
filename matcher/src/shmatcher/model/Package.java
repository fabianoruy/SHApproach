package shmatcher.model;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents a Package (Ontology, Standard Model or ICM). */
public abstract class Package {
    private String id;
    private String name;
    private String definition;
    private Diagram diagram;
    private IPackage astahPack;

    public Package(IPackage astahPack) {
	this.id = astahPack.getId();
	this.name = astahPack.getName();
	this.definition = astahPack.getDefinition();
	this.astahPack = astahPack;
    }

    public String getId() {
	return id;
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
    
    @Override
    public boolean equals(Object other) {
	return (other instanceof Package && ((Package) other).id.equals(this.id));
    }


}