package shmapper.model;

/* Represents an abstract Match between an Element and a Concept/Element. */
public abstract class Match extends SerializableObject {
	private static final long serialVersionUID = -8851562653124263290L;
	private Element source;
	private MatchType type;
	private String comment;
	private boolean deduced;
	private Mapping mapping;

	public Match(Element elem, MatchType type, String comm) {
		this.source = elem;
		this.type = type;
		this.comment = comm;
		this.deduced = false;
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