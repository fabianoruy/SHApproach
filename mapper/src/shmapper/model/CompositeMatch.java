package shmapper.model;

import java.util.List;

/* Represents a composition of Matches for the same source. */
public class CompositeMatch extends Match {
	private static final long	serialVersionUID	= -6126451502312008378L;
	private List<SimpleMatch>	components;

	public CompositeMatch(Element source, Coverage cover, String comm, List<SimpleMatch> matches) {
		super(source, cover, comm);
		this.components = matches;
	}

	public List<SimpleMatch> getComponents() {
		return components;
	}

	public String getTarget() {
		String text = "";
		for (SimpleMatch match : components) {
			text += match.getTarget() + " + ";
		}
		text = "(" + text.substring(0, text.length() - 3) + ")";
		return text;
	}

	@Override
	public String toString() {
		return getSource() + " " + getCoverage().getAbbreviation() + " " + getTarget();
	}

	@Override
	public boolean equals(Object other) {
		// Equal if they have same source in the same Mapping.
		CompositeMatch omatch;
		if (other instanceof CompositeMatch) {
			omatch = (CompositeMatch) other;
			return (this.getSource().equals(omatch.getSource()) && this.getMapping().equals(omatch.getMapping()));
		}
		return false;
	}

}
