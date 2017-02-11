package shmapper.model;

/* Represents a Match between an Element and a Concept/Element. */
public class SimpleMatch extends Match {
	private static final long	serialVersionUID	= 2134549264691121407L;
	private Notion				target;

	public SimpleMatch(Element source, Notion target, MatchType type, String comm) {
		super(source, type, comm);
		this.target = target;
	}

	public Notion getTarget() {
		return target;
	}

	public String getType() {
		return "E [" + getMatchType().name().charAt(0) + "] " + target.getClass().getName().charAt(0);
	}

	@Override
	public String toString() {
		return getSource() + " " + getMatchType().getAbbreviation() + " " + target;
	}

	@Override
	public boolean equals(Object other) {
		// Equal if they have same source and target.
		// SEE use of contains method before changing it!
		SimpleMatch omatch;
		if (other instanceof SimpleMatch) {
			omatch = (SimpleMatch) other;
			return (this.getSource().equals(omatch.getSource()) && this.target.equals(omatch.target));
		}
		return false;
	}

}