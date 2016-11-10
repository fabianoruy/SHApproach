package shmapper.model;

import com.change_vision.jude.api.inf.model.IDiagram;

/* Represents a Diagram (for the Seon View, a Standard Content Model or the ICM). */
public class Diagram {
    private String name;
    private String description;
    private DiagramType type;
    private IDiagram astahDiagram;

    /* Seon View, Standard Structural Model, Integrated Structural Model, Standard Content Model, Integrated Content
     * Model. */
    public static enum DiagramType {
	SEONVIEW, SSM, ISM, SCM, ICM
    }

    public Diagram(String name, String desc, DiagramType type, IDiagram astahDiag) {
	this.name = name;
	this.description = desc;
	this.type = type;
	this.astahDiagram = astahDiag;
    }

    public String getName() {
	return name;
    }

    public String getDescription() {
	return description;
    }

    public DiagramType getType() {
	return type;
    }

    public IDiagram getAstahDiagram() {
	return astahDiagram;
    }

    @Override
    public String toString() {
	return this.name;
    }

}