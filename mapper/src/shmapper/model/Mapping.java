package shmapper.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/* Represents an abstract Mapping between a Model and an Ontology (vertical) or another Model (horizontal). */
public abstract class Mapping {
	private String			id;
	private StandardModel	base;
	private boolean			structural;
	private List<Match>		matches;
	private MappingStatus	status;

	public static enum MappingStatus {
		PLANNED, STARTED, FINISHED
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

	public boolean isStructural() {
		return structural;
	}

	public void setStructural(boolean structural) {
		this.structural = structural;
	}

	/* Returns the coverage of the matchs over the Standard's Elements. */
	public int getCoverage() {
		// coverage (%): baseModel.elements.([E] + [P] + [W]/2 + [I]/2) / baseModel.elements;
		int all = getBase().getElements().size();
		int partially = getPartiallyCoveredElements().size();
		int noncovered = getNonCoveredElements().size();
		int fully = all - partially - noncovered;
		double coverage = ((partially / 2.0 + fully) / all) * 100;
		// System.out.println(this + ": All(" + all + "), Full(" + fully + "), Part(" + partially + "), Non(" +
		// noncovered + "): Cover(" + coverage + "%)");
		return (int) Math.round(coverage);
	}

	public MappingStatus getStatus() {
		return status;
	}
	
	public List<Match> getMatches() {
		return this.matches;
	}

	/* Returns the Match with the id parameter. */
	public Match getMatchById(String matchId) {
		for (Match match : matches) {
			if (match.getId().equals(matchId)) {
				return match;
			}
		}
		return null;
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

	/* Returns all the composite matches of the mapping. */
	public List<CompositeMatch> getCompositeMatches() {
		List<CompositeMatch> cmatches = new ArrayList<CompositeMatch>();
		for (Match match : matches) {
			if (match instanceof CompositeMatch) {
				cmatches.add((CompositeMatch) match);
			}
		}
		return cmatches;
	}

	/* Returns the simple matches which elem is the source. */
	public List<SimpleMatch> getSimpleMatchesBySource(Element source) {
		List<SimpleMatch> smatches = new ArrayList<SimpleMatch>();
		for (Match match : matches) {
			if (match instanceof SimpleMatch && match.getSource().equals(source)) {
				smatches.add((SimpleMatch) match);
			}
		}
		return smatches;
	}
	
	/* Returns the simple matches which elem is the target. */
	public List<SimpleMatch> getSimpleMatchesByTarget(Notion target) {
		List<SimpleMatch> smatches = new ArrayList<SimpleMatch>();
		for (Match match : matches) {
			if (match instanceof SimpleMatch && ((SimpleMatch) match).getTarget().equals(target)) {
				smatches.add((SimpleMatch) match);
			}
		}
		return smatches;
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

	/* Returns the single composite match, in this mapping, for the simple match. */
	public CompositeMatch getCompositeMatch(SimpleMatch match) {
		for (CompositeMatch cmatch : getCompositeMatches()) {
			for (SimpleMatch smatch : cmatch.getMatches()) {
				// is the simple match part of a composite match?
				if (match.equals(smatch)) {
					return cmatch;
				}
			}
		}
		return null;
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

		// Starting the mapping, if first Match
		if (status == MappingStatus.PLANNED) {
			status = MappingStatus.STARTED;
		}
	}
	
	/* Removes a Match to the Mapping. */
	public void removeMatch(Match rmatch) {
		matches.remove(rmatch);
	}

	public void finishMapping() {
		this.status = MappingStatus.FINISHED;
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