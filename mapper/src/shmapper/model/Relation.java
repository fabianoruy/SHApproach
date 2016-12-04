package shmapper.model;

import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IAttribute;
import com.change_vision.jude.api.inf.model.IMultiplicityRange;

/* Represents the Relations between Concepts or between Elements. */
public class Relation extends SerializableObject {
	private static final long	serialVersionUID	= -5299528956366422964L;
	private String				name;
	private String				definition;
	private String				stereotype;
	private boolean				composition;
	private Notion				source;
	private Notion				target;
	private RelationType		type;
	private String				sourceMult;
	private String				targetMult;
	private boolean				original;

	public static enum RelationType {
		SAMEAS("the SAME as"),
		PARTOF("PART of"),
		INTERSECTION("INTERSECTED with");

		private final String text;

		private RelationType(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	/** Default constructor, for original relations. */
	public Relation(Notion source, Notion target, IAssociation assoc) {
		IAttribute firstEnd = assoc.getMemberEnds()[0];
		IAttribute secondEnd = assoc.getMemberEnds()[1];

		this.name = assoc.getName();
		this.definition = assoc.getDefinition();
		if (assoc.getStereotypes().length > 0) {
			this.stereotype = assoc.getStereotypes()[0]; // only the first for while
		}
		this.composition = (firstEnd.isComposite() || firstEnd.isAggregate());
		if (composition)
			this.type = RelationType.PARTOF;

		this.sourceMult = getMultiplicity(firstEnd);
		this.targetMult = getMultiplicity(secondEnd);

		this.source = source;
		this.target = target;
		this.original = true;
	}

	/** Alternative constructor for asserted relations. */
	public Relation(Element source, Element target, RelationType type) {
		this.source = source;
		this.target = target;
		this.type = type;
		this.original = false;
	}

	public String getName() {
		return this.name;
	}

	public String getDefinition() {
		return this.definition;
	}

	public String getStereotype() {
		return this.stereotype;
	}

	public boolean isComposition() {
		return composition;
	}

	public Notion getSource() {
		return this.source;
	}

	public Notion getTarget() {
		return this.target;
	}

	public String getSourceMultiplicity() {
		return this.sourceMult;
	}

	public String getTargetMultiplicity() {
		return this.targetMult;
	}

	public boolean isOriginal() {
		return original;
	}

	public RelationType getType() {
		// TODO: remove if for new initiatives
		if (composition)
			this.type = RelationType.PARTOF;

		return type;
	}

	@Override
	public String toString() {
		String rname = name;
		String ster = "";
		if (getType() != null)
			rname = type.toString();
		if (stereotype != null)
			ster = "  &lt&lt" + stereotype + "&gt&gt";
		return source.getName() + " " + rname + " " + target.getName() + ster;
		// String smult = " ";
		// String tmult = " ";
		// if (!sourceMult.isEmpty()) smult = " (" + sourceMult + ") ";
		// if (!targetMult.isEmpty()) tmult = " (" + targetMult + ") ";
		// return source.getName() + smult + rname + tmult + target.getName() + ster;
	}

	/* Returns the multiplicity of an end in text format (n..m). */
	private String getMultiplicity(IAttribute iAttrib) {
		IMultiplicityRange imult;
		if (iAttrib.getMultiplicity().length > 0) {
			imult = iAttrib.getMultiplicity()[0];
			int lower = imult.getLower();
			int upper = imult.getUpper();
			if (lower == IMultiplicityRange.UNDEFINED)
				return "";
			if (lower == IMultiplicityRange.UNLIMITED)
				return "*";
			if (upper == IMultiplicityRange.UNDEFINED)
				return lower + "";
			if (upper == IMultiplicityRange.UNLIMITED)
				return lower + "..*";
			return lower + ".." + upper;
		}
		return "";
	}

}