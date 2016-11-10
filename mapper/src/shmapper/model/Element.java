package shmapper.model;

import com.change_vision.jude.api.inf.model.IClass;

/* Represents an Element from a Standard Model. */
public class Element extends Notion {
    private StandardModel standard;

    public Element(StandardModel standard, IClass astahClass) {
	super(astahClass);
	this.standard = standard;
    }

    public StandardModel getStandardModel() {
	return standard;
    }

    public boolean isIntegrated() {
	return standard == null;
    }

}