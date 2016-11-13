package shmapper.model;

import java.util.ArrayList;
import java.util.List;

/* Represents an abstract Mapping between a Model and an Ontology (vertical) or another Model (horizontal). */
public abstract class Mapping {
	private StandardModel	base;
	private List<Match>		matches;
	private MappingStatus	status;

	public static enum MappingStatus {
		PLANNED, STARTED, FINISHED
	}

	public Mapping(StandardModel base) {
		this.base = base;
		this.status = MappingStatus.PLANNED;
		this.matches = new ArrayList<Match>();
	}

	public StandardModel getBase() {
		return base;
	}

	public abstract Package getTarget();

	public MappingStatus getStatus() {
		return status;
	}

	public List<Match> getMatches() {
		return this.matches;
	}

	/* Returns the simple matches which elem is the source. */
	public List<SimpleMatch> getSimpleMatches(Element elem) {
		List<SimpleMatch> elemMatches = new ArrayList<SimpleMatch>();
		for (Match match : matches) {
			if (match instanceof SimpleMatch && match.getSource().equals(elem)) {
				elemMatches.add((SimpleMatch) match);
			}
		}
		return elemMatches;
	}

	/* Returns the single composite match, in this mapping, within elem is the source. */
	public CompositeMatch getCompositeMatch(Element elem) {
		for (Match match : matches) {
			if (match instanceof CompositeMatch && match.getSource().equals(elem)) {
				return (CompositeMatch) match;
			}
		}
		return null;
	}

	/* Adds a match to the mapping. */
	public void addMatch(Match match) {
		// Adds a simple match to the mapping.
		if (match instanceof SimpleMatch) {
			this.matches.add(match);
		} else {
			// Adds a composite match to the mapping, replacing the previous with the same source.
			Match previous = getCompositeMatch(match.getSource());
			matches.remove(previous);
			matches.add(match);
		}
		match.setMapping(this);
	}

	public List<SimpleMatch> getSimpleMatches() {
		List<SimpleMatch> smatches = new ArrayList<SimpleMatch>();
		for (Match match : matches) {
			if (match instanceof SimpleMatch) {
				smatches.add((SimpleMatch) match);
			}
		}
		return smatches;
	}

	@Override
	public boolean equals(Object other) {
		Mapping omap;
		if (other instanceof Mapping) {
			omap = (Mapping) other;
			return (this.getBase().equals(omap.getBase()) && this.getTarget().equals(omap.getTarget()));
		}
		return false;
	}

}