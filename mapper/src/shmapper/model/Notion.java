package shmapper.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IClass;

/* Represents a Notion (Element or Concept). */
public abstract class Notion extends SerializableObject {
	private static final long	serialVersionUID	= -6465796034657397883L;
	private String				name;
	private String				definition;
	private String				stereotype;
	private UFOType				ufotype;
	private List<Notion>		generalizations;
	private List<Relation>		relations;

	public static enum UFOType {
		EVENT, OBJECT, AGENT, MOMENT, SITUATION;
	}

	/* General constructor. */
	public Notion(IClass astahClass) {
		super.setId(astahClass.getId());
		this.name = astahClass.getName();
		this.definition = astahClass.getDefinition();
		if (astahClass.getStereotypes().length > 0) {
			stereotype = astahClass.getStereotypes()[0]; // only the first for while
		} else this.stereotype = "";
		this.generalizations = new ArrayList<Notion>();
		this.relations = new ArrayList<Relation>();
	}

	/* Protected constructor for new ICM Elements (no astahClass). */
	protected Notion(String name, String def, Notion type) {
		this.name = name;
		this.definition = def;
		this.generalizations = new ArrayList<Notion>();
		this.addGeneralization(type);
		this.relations = new ArrayList<Relation>();
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

	/* Returns the UFO type of this Notion, or of the closer ancestral. */
	public UFOType getIndirectUfotype() {
		if (ufotype != null)
			return ufotype;
		if (!generalizations.isEmpty())
			// one branch is enough (assuming all braches have the same ufotype)
			return generalizations.get(0).getIndirectUfotype();
		return null;
	}

	/* Returns the basetypes (first found in each branch) of this Notion. */
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

	/* Returns the basetypes (all found in each branch) of this Notion. */
	public List<Notion> getAllBasetypes() {
		ArrayList<Notion> basetypes = new ArrayList<Notion>();
		for (Notion general : getGeneralizations()) {
			if (general.isBasetype()) {
				basetypes.add(general);
				basetypes.addAll(general.getBasetypes());
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

}
