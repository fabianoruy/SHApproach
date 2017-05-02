package shmapper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

	/** Sorts the elements according to their position in the diagram (top to down). */
	public void sortElementsByPresentation() {
		final List<NotionPosition> positions = this.getDiagram().getPositions();
		Comparator<Element> comparator = new Comparator<Element>() {
			public int compare(Element e1, Element e2) {
				int pos1 = Integer.MAX_VALUE, pos2 = Integer.MAX_VALUE;
				for (NotionPosition pos : positions) {
					if (pos.getNotion().equals(e1)) {
						pos1 = pos.getYpos();
						break;
					}
				}
				for (NotionPosition pos : positions) {
					if (pos.getNotion().equals(e2)) {
						pos2 = pos.getYpos();
						break;
					}
				}
				return pos1 - pos2;
			}
		};
		Collections.sort(elements, comparator);
	}

}