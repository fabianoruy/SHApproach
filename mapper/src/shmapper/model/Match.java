package shmapper.model;

/* Represents an abstract Match between an Element and a Concept/Element. */
public abstract class Match extends SerializableObject {
	private static final long	serialVersionUID	= -8851562653124263290L;
	private Element				source;
	private Coverage			coverage;
	private String				comment;
	private boolean deduced;
	private Mapping				mapping;

	public Match(Element elem, Coverage cover, String comm) {
		this.source = elem;
		this.coverage = cover;
		this.comment = comm;
		this.deduced = false;
	}

	public Element getSource() {
		return source;
	}

	public Coverage getCoverage() {
		return coverage;
	}

	public String getComment() {
		return comment;
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