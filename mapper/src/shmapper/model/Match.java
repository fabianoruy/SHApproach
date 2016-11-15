package shmapper.model;

import java.util.UUID;

/* Represents an abstract Match between an Element and a Concept/Element. */
public abstract class Match {
	private String		id;
	private Element		source;
	private Coverage	coverage;
	private String		comment;
	private Mapping		mapping;

	public Match(Element elem, Coverage cover, String comm) {
		this.id = UUID.randomUUID().toString();
		this.source = elem;
		this.coverage = cover;
		this.comment = comm;
	}

	public String getId() {
		return id;
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