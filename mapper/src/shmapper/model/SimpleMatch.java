package shmapper.model;

/* Represents a Match between an Element and a Concept/Element. */
public class SimpleMatch extends Match {
	private static final long	serialVersionUID	= 2134549264691121407L;
	private Notion				target;

	public SimpleMatch(Element source, Notion target, Coverage cover, String comm) {
		super(source, cover, comm);
		this.target = target;
	}

	public Notion getTarget() {
		return target;
	}

	public String getType() {
		return "E [" + getCoverage().name().charAt(0) + "] " + target.getClass().getName().charAt(0);
	}

	@Override
	public String toString() {
		return getSource() + " [" + getCoverage().name().charAt(0) + "] " + target;
	}

	@Override
	public boolean equals(Object other) {
		// Equal if they have same source and target.
		SimpleMatch omatch;
		if (other instanceof SimpleMatch) {
			omatch = (SimpleMatch) other;
			return (this.getSource().equals(omatch.getSource()) && this.target.equals(omatch.target));
		}
		return false;
	}

}