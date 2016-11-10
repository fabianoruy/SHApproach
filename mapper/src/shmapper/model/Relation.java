package shmapper.model;

/* Represents the Relations between Concepts or between Elements. */
public class Relation {
	private String	name;
	private String	definition;
	private String	stereotype;
	private boolean	composition;
	private Notion	source;
	private Notion	target;
	private String	sourceMult;
	private String	targetMult;
	private boolean	original;

	public Relation(String name, String def, String ster, boolean composition, Notion source, Notion target, String smult, String tmult) {
		this.name = name;
		this.definition = def;
		this.stereotype = ster;
		this.composition = composition;
		this.source = source;
		this.target = target;
		this.sourceMult = smult;
		this.targetMult = tmult;
		this.original = true;
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

	public void setOriginal(boolean original) {
		this.original = original;
	}

	public boolean isOriginal() {
		return original;
	}

	@Override
	public String toString() {
		String rname = name;
		String ster = "";
		if (composition) rname = "<>--" + name;
		if (stereotype != null) ster = "  &lt&lt" + stereotype + "&gt&gt";
		return source.getName() + " " + rname + " " + target.getName() + ster;
	}

}