package shmapper.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IClass;

/* Represents a Notion (Element or Concept). */
public abstract class Notion {
    private String id;
    private String name;
    private String definition;
    private String stereotype;
    private List<Notion> generalizations;
    private List<Relation> relations;
    private IClass astahClass;

    public Notion(IClass astahClass) {
	this.id = astahClass.getId();
	this.name = astahClass.getName();
	this.definition = astahClass.getDefinition();
	if (astahClass.getStereotypes().length > 0)
	    this.stereotype = astahClass.getStereotypes()[0]; // only the first for while
	else
	    this.stereotype = "";
	this.generalizations = new ArrayList<Notion>();
	this.relations = new ArrayList<Relation>();
	this.astahClass = astahClass;
    }

    public String getId() {
	return id;
    }

    public String getName() {
	return name;
    }

    public String getDefinition() {
	return definition;
    }

    public String getStereotype() {
	return stereotype;
    }

    public List<Notion> getGeneralizations() {
	return generalizations;
    }

    public void addGeneralization(Notion notion) {
	this.generalizations.add(notion);
    }

    public List<Relation> getRelations() {
	return relations;
    }

    public void addRelation(Relation relation) {
	if (!relations.contains(relation)) {
	    this.relations.add(relation);
	}
    }

    public IClass getAstahClass() {
	return astahClass;
    }

    @Override
    public String toString() {
	return this.name;
    }

    @Override
    public boolean equals(Object other) {
	return (other instanceof Notion && ((Notion) other).id.equals(this.id));
    }

}
