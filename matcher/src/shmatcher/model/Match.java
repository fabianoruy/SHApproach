package shmatcher.model;

/* Represents an abstract Match between an Element and a Concept/Element. */
public abstract class Match {
	private Element		source;
	private Coverage	coverage;
	private String		comment;

	public Match(Element elem, Coverage cover, String comm) {
		this.source = elem;
		this.coverage = cover;
		this.comment = comm;
	}

	public Notion getSource() {
		return source;
	}

	public Coverage getCoverage() {
		return coverage;
	}

	public String getComment() {
		return comment;
	}

}