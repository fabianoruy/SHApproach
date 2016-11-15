package shmapper.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/* Represents an abstract Mapping between a Model and an Ontology (vertical) or another Model (horizontal). */
public abstract class Mapping {
	private String			id;
	private StandardModel	base;
	private List<Match>		matches;
	private MappingStatus	status;

	public static enum MappingStatus {
		PLANNED, AUTHORIZED, STARTED, FINISHED
	}

	public Mapping(StandardModel base) {
		this.id = UUID.randomUUID().toString();
		this.base = base;
		this.status = MappingStatus.PLANNED;
		this.matches = new ArrayList<Match>();
	}

	public String getId() {
		return this.id;
	}

	public StandardModel getBase() {
		return base;
	}

	public abstract Package getTarget();

	public abstract int getCoverage();

	public MappingStatus getStatus() {
		return status;
	}

	public List<Match> getMatches() {
		return this.matches;
	}

	/* Returns all the simple matches of the mapping. */
	public List<SimpleMatch> getSimpleMatches() {
		List<SimpleMatch> smatches = new ArrayList<SimpleMatch>();
		for (Match match : matches) {
			if (match instanceof SimpleMatch) {
				smatches.add((SimpleMatch) match);
			}
		}
		return smatches;
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

	/* Returns the current non covered elements of the mapping. */
	public List<Element> getNonCoveredElements() {
		List<Element> elems = new LinkedList<Element>(base.getElements());
		for (Match match : matches) {
			elems.remove(match.getSource());
		}
		return elems;
	}

	/* Returns the current partially covered elements of the mapping. */
	public List<Element> getPartiallyCoveredElements() {
		List<Element> elems = new LinkedList<Element>(base.getElements());
		// removing non covered elements
		elems.removeAll(getNonCoveredElements());
		for (Match match : matches) {
			Coverage cover = match.getCoverage();
			// removing elements with Equivalent or Partial, remmaining the ones with ONLY Wider/Intersection coverage.
			if (cover == Coverage.EQUIVALENT || cover == Coverage.PARTIAL) {
				elems.remove(match.getSource());
			}
		}
		return elems;
	}

	/* Returns the current fully covered elements of the mapping. */
	public List<Element> getFullyCoveredElements() {
		List<Element> elems = new ArrayList<Element>();
		for (Match match : matches) {
			Coverage cover = match.getCoverage();
			// adding all elements with Equivalent or Partial matches
			if (cover == Coverage.EQUIVALENT || cover == Coverage.PARTIAL) {
				elems.add(match.getSource());
			}
		}
		return elems;
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

	@Override
	public String toString() {
		return getBase() + " <--> " + getTarget();
	}

}