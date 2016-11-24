package shmapper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents the Integrated Content Model (ICM) of an Initiative. */
public class IntegratedModel extends Model {
	private static final long	serialVersionUID	= 8909961454762309968L;
	private String				description;
	private List<Concept>		concepts;

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

	public List<Notion> getNotionsOrdered() {
		List<Notion> notions = new ArrayList<Notion>();
		notions.addAll(concepts);
		notions.addAll(getElements());
		Collections.sort(notions, new Comparator<Notion>() {
			public int compare(Notion notion, Notion other) {
				int result = notion.getIndirectUfotype().toString().compareTo(other.getIndirectUfotype().toString());
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