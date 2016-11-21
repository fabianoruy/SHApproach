package shmapper.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IClass;

/* Represents a Notion (Element or Concept). */
public abstract class Notion {
	private String			id;
	private String			name;
	private String			definition;
	private String			stereotype;
	private UFOType			ufotype;
	private List<Notion>	generalizations;
	private List<Relation>	relations;

	public static enum UFOType {
		EVENT, OBJECT, AGENT, MOMENT, SITUATION;
	}

	public Notion(IClass astahClass) {
		this.id = astahClass.getId();
		this.name = astahClass.getName();
		this.definition = astahClass.getDefinition();
		if (astahClass.getStereotypes().length > 0) {
			stereotype = astahClass.getStereotypes()[0]; // only the first for while
		} else this.stereotype = "";
		this.generalizations = new ArrayList<Notion>();
		this.relations = new ArrayList<Relation>();
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

	public UFOType getUfotype() {
		return ufotype;
	}

	public void setUfotype(UFOType type) {
		this.ufotype = type;
	}

	public List<Notion> getGeneralizations() {
		return generalizations;
	}

	public void addGeneralization(Notion notion) {
		this.generalizations.add(notion);
	}

	// /* Returns the highest level type of this notion. */
	// public Notion getBasetype() {
	// // TODO: improve to scape UFO
	// Notion basetype = this;
	// while (basetype.getGeneralizations().size() > 0) {
	// basetype = basetype.getGeneralizations().get(0);
	// }
	// return basetype;
	// }

	/* Returns all basetypes of this element. */
	public List<Notion> getBasetypes() {
		ArrayList<Notion> basetypes = new ArrayList<Notion>();
		for (Notion general : getGeneralizations()) {
			if (general.isBasetype()) {
				basetypes.add(general);
			} else {
				basetypes.addAll(general.getBasetypes());
			}
		}
		return basetypes;
	}

	public abstract boolean isBasetype();

	public abstract Package getPackage();

	public List<Relation> getRelations() {
		return relations;
	}

	public void addRelation(Relation relation) {
		if (!relations.contains(relation)) {
			this.relations.add(relation);
		}
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
