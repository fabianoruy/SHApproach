package shmapper.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

import shmapper.model.Notion.UFOType;

/* Represents a Model with Elements. */
public abstract class Model extends Package<Element> {
	private static final long	serialVersionUID	= 5300026087585445762L;
	private boolean				structural;
	private List<Element>		elements;

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
	
	@Override
	public List<Element> getNotions() {
		return getElements();
	}

	public void addElement(Element elem) {
		this.elements.add(elem);
	}
	
	/* Removes an Element from this Model. */
	public void removeElement(Element elem) {
		this.elements.remove(elem);
	}

	public List<Element> getElementsByUfotype(UFOType type) {
		List<Element> typedElements = new ArrayList<Element>();
		for (Element elem : elements) {
			if (elem.getIndirectUfotype() == type) {
				typedElements.add(elem);
			}
		}
		return typedElements;
	}

	public Element getElementByName(String name) {
		for (Element element : elements) {
			if (element.getName().equals(name)) {
				return element;
			}
		}
		return null;
	}

}