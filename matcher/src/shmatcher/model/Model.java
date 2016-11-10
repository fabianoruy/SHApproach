package shmatcher.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents a Model with Elements. */
public abstract class Model extends Package {
    private boolean structural;
    private List<Element> elements;

    public Model(boolean struct, IPackage astahPack) {
	super(astahPack);
	this.structural = struct;
	this.elements = new ArrayList<Element>();
    }

    public boolean isStructural() {
	return structural;
    }

    public List<Element> getElements() {
	return this.elements;
    }

    public void addElement(Element elem) {
	this.elements.add(elem);
    }

}