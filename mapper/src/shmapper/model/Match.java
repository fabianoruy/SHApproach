package shmapper.model;

/* Represents an abstract Match between an Element and a Concept/Element. */
public abstract class Match extends SerializableObject {
	private static final long	serialVersionUID	= -8851562653124263290L;
	private Element				source;
	private MatchType			type;
	private String				comment;
	private Coverage			coverage			= Coverage.UNDEFINED;
	private boolean				deduced;
	private Mapping				mapping;

	public static enum Coverage {
		PARTIAL(1 / 3.0), LARGE(2 / 3.0), FULL(1.0), UNDEFINED(1 / 2.0);

		private final double value;

		private Coverage(final double value) {
			this.value = value;
		}

		public double getValue() {
			return value;
		}
	}

	public Match(Element elem, MatchType type, String comm) {
		this.source = elem;
		this.type = type;
		this.comment = comm;
		this.deduced = false;
		if (type == MatchType.EQUIVALENT || type == MatchType.PARTIAL)
			this.coverage = Coverage.FULL;
	}

	public Element getSource() {
		return source;
	}

	public MatchType getMatchType() {
		return type;
	}

	public String getComment() {
		if (comment != null)
			return comment.replaceAll("'|\"", "");
		return null;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Coverage getCoverage() {
		return coverage;
	}

	public void setCoverage(Coverage coverage) {
		this.coverage = coverage;
	}

	public boolean isDeduced() {
		return deduced;
	}

	public void setDeduced(boolean deduced) {
		this.deduced = deduced;
	}

	public Mapping getMapping() {
		return mapping;
	}

	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}

}