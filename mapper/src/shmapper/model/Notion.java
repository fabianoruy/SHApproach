package shmapper.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IClass;

import shmapper.model.Relation.RelationType;

/** Represents a Notion (Element or Concept). */
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

	/** General constructor. */
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

	/** Protected constructor for new ICM Elements (no astahClass). */
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
		if (definition != null)
			return definition.replaceAll("'|\"", "");
		return null;
	}

	public void setDefinition(String def) {
		this.definition = def;
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

	/** Returns the UFO type of this Notion, or of the closer ancestral. */
	public UFOType getIndirectUfotype() {
		if (ufotype != null)
			return ufotype;
		if (!generalizations.isEmpty()) {
			// one branch is enough (assuming all braches have the same ufotype)
			return generalizations.get(0).getIndirectUfotype();
		}
		//TODO remove
		if (this.name.equals("Work Unit")) {
			this.setUfotype(UFOType.EVENT);
			return UFOType.EVENT;
		}
		System.out.println("#No UFOType# " + this);
		
		return null;
	}

	/** Returns the basetypes (first found in each branch) of this Notion. */
	public List<Notion> getBasetypes() {
		ArrayList<Notion> basetypes = new ArrayList<Notion>();
		for (Notion general : getGeneralizations()) {
			if (general == null) {
				System.out.println("Null Generalization: " + this);
				System.out.flush();
			} else {
				if (general.isBasetype()) {
					basetypes.add(general);
				} else {
					basetypes.addAll(general.getBasetypes());
				}
			}
		}
		return basetypes;
	}

	/** Returns the basetypes (all found in each branch) of this Notion. */
	public List<Notion> getAllBasetypes() {
		ArrayList<Notion> basetypes = new ArrayList<Notion>();
		for (Notion general : getGeneralizations()) {
			if (general.isBasetype()) {
				basetypes.add(general);
				basetypes.addAll(general.getAllBasetypes());
			} else {
				basetypes.addAll(general.getAllBasetypes());
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

	/** Returns the Whole of this Notion, if exists. */
	public Notion getWhole() {
		// considering a unique whole
		for (Relation relation : relations) {
			if (relation.getType() == RelationType.PARTOF && relation.getTarget().equals(this))
				return relation.getSource();
		}
		return null;
	}

	/** Returns if THIS Notion is (direct or indirect) Part of the given notion. */
	public boolean isIndirectPartOf(Notion onotion) {
		// Is THIS part of Other?
		// TODO: do recursivelly
		for (Relation relation : relations) {
			if (relation.getType() == RelationType.PARTOF) {
				if (relation.getSource().equals(onotion) && relation.getTarget().equals(this)) {
					return true;
				}
			}
		}
		return false;
	}

	/** Returns if this Notion (directly) Intersects with the given notion. */
	public boolean intersectsWith(Notion onotion) {
		for (Relation relation : relations) {
			if (relation.getType() == RelationType.INTERSECTION) {
				if ((relation.getSource().equals(onotion) && relation.getTarget().equals(this)) || (relation.getSource().equals(this) && relation.getTarget().equals(onotion))) {
					return true;
				}
			}
		}
		return false;
	}

	/** Returns if this Notion is (directly) Equivalent to the given notion. */
	public boolean isEquivalentTo(Notion onotion) {
		for (Relation relation : relations) {
			if (relation.getType() == RelationType.EQUIVALENT) {
				if ((relation.getSource().equals(onotion) && relation.getTarget().equals(this)) || (relation.getSource().equals(this) && relation.getTarget().equals(onotion))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
