package shmapper.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents the Integrated Content Model (ICM) of an Initiative. */
public class IntegratedModel extends Model {
    private String description;
    private List<Concept> concepts;

    public IntegratedModel(boolean struct, IPackage astahPack) {
	super(struct, astahPack);
	this.concepts = new ArrayList<Concept>();
    }

    public String getDescription() {
	return description;
    }

    public List<Concept> getConcepts() {
	return concepts;
    }

    public void addConcept(Concept concept) {
	this.concepts.add(concept);
    }

    public void setDescription(String description) {
	this.description = description;
    }

}