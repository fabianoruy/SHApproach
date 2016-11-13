package shmapper.model;

/* Represents an abstract Match between an Element and a Concept/Element. */
public abstract class Match {
	private Element		source;
	private Coverage	coverage;
	private String		comment;
	private Mapping		mapping;

	public Match(Element elem, Coverage cover, String comm) {
		this.source = elem;
		this.coverage = cover;
		this.comment = comm;
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

	public Mapping getMapping() {
		return mapping;
	}
	
	public void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}

}