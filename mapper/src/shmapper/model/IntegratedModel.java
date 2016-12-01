package shmapper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

import shmapper.model.Notion.UFOType;

/* Represents the Integrated Content Model (ICM) of an Initiative. */
public class IntegratedModel extends Model {
	private static final long serialVersionUID = 8909961454762309968L;
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
	
	/* Returns only the new IM elements, not provided by astah. */
//	public List<Element> getNewElements() {
//		List<Element> newelems = new ArrayList<Element>();
//		for (Element elem : getElements()) {
//			if(distinction criterion) { //TODO
//				newelems.add(elem);
//			}			
//		}
//		return newelems;
//	}

	/* Returns the IM Notions ordered by UFO Type. */
	public List<Notion> getNotionsOrdered() {
		List<Notion> notions = new ArrayList<Notion>();
		notions.addAll(concepts);
		notions.addAll(getElements());
		Collections.sort(notions, new Comparator<Notion>() {
			public int compare(Notion notion, Notion other) {
				UFOType ntype = notion.getIndirectUfotype();
				UFOType otype = other.getIndirectUfotype();
				if(ntype == null) return -1;
				if(otype == null) return 1;
				int result = ntype.toString().compareTo(otype.toString());
				if (result == 0)
					return notion.getName().compareToIgnoreCase(other.getName());
				return result;
			}
		});
		return notions;
	}

	public void addConcept(Concept concept) {
		this.concepts.add(concept);
	}

	public void setDescription(String description) {
		this.description = description;
	}

}