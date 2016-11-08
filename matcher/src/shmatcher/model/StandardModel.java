package shmatcher.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents a Model extracted from a Standard. */
public class StandardModel extends Package {
	private boolean			structural;
	private String scope;
	private List<Element>	elements;

	public StandardModel(String name, String def, boolean struct, IPackage astahPack) {
		super(name, def, astahPack);
		this.structural = struct;
		this.elements = new ArrayList<Element>();
	}

	public boolean isStructural() {
		return structural;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public List<Element> getElements() {
		return this.elements;
	}

	public void addElement(Element elem) {
		this.elements.add(elem);
	}

}