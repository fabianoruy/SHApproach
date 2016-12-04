package shmapper.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import shmapper.model.Element.CoverateSituation;
import shmapper.model.Notion.UFOType;
import shmapper.model.Relation.RelationType;

/** Represents an abstract Mapping between a Model and an Ontology (vertical) or another Model (horizontal). */
public abstract class Mapping extends SerializableObject {
	private static final long	serialVersionUID	= 1581632337297022767L;
	private StandardModel		base;
	private boolean				structural;
	private List<Match>			matches;
	private MappingStatus		status;

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

	public boolean isStructural() {
		return structural;
	}

	public void setStructural(boolean structural) {
		this.structural = structural;
	}

	/** Returns the coverage of the matchs over the Standard's Elements (base). */
	public int getCoverage() {
		// coverage (%): baseModel.elements.([E] + [P] + [W]/2 + [I]/2) / baseModel.elements;
		int all = getBase().getElements().size();
		int partially = getPartiallyCoveredElements().size();
		int noncovered = getNonCoveredElements().size();
		int fully = all - partially - noncovered;
		double coverage = ((partially * 0.35 + fully) / all) * 100;
		if (fully == all) {
			this.status = MappingStatus.FINISHED;
			return 100;
		} else if (coverage > 0.1) {
			this.status = MappingStatus.STARTED;
		} else {
			this.status = MappingStatus.PLANNED;
		}
		// System.out.println(this + ": All(" + all + "), Full(" + fully + "), Part(" + partially + "), Non("
		// +noncovered + "): Cover(" + coverage + "%)");
		return (int) Math.round(coverage);
	}

	public MappingStatus getStatus() {
		return status;
	}

	public List<Match> getMatches() {
		return this.matches;
	}

	/** Returns the Match with the id parameter. */
	public Match getMatchById(String matchId) {
		for (Match match : matches) {
			if (match.getId().equals(matchId)) {
				return match;
			}
		}
		return null;
	}

	/** Returns all the simple matches of the mapping. */
	public List<SimpleMatch> getSimpleMatches() {
		List<SimpleMatch> smatches = new ArrayList<SimpleMatch>();
		for (Match match : matches) {
			if (match instanceof SimpleMatch) {
				smatches.add((SimpleMatch) match);
			}
		}
		return smatches;
	}

	/** Returns all the composite matches of the mapping. */
	public List<CompositeMatch> getCompositeMatches() {
		List<CompositeMatch> cmatches = new ArrayList<CompositeMatch>();
		for (Match match : matches) {
			if (match instanceof CompositeMatch) {
				cmatches.add((CompositeMatch) match);
			}
		}
		return cmatches;
	}

	/** Returns the matches (simple or composite) which elem is the source. */
	public List<Match> getMatchesBySource(Element source) {
		List<Match> smatches = new ArrayList<Match>();
		for (Match match : matches) {
			if (match.getSource().equals(source)) {
				smatches.add(match);
			}
		}
		return smatches;
	}

	/** Returns the simple matches which elem is the source. */
	public List<SimpleMatch> getSimpleMatchesBySource(Element source) {
		List<SimpleMatch> smatches = new ArrayList<SimpleMatch>();
		for (Match match : matches) {
			if (match instanceof SimpleMatch && match.getSource().equals(source)) {
				smatches.add((SimpleMatch) match);
			}
		}
		return smatches;
	}

	/** Returns the simple matches which elem is the target. */
	public List<SimpleMatch> getSimpleMatchesByTarget(Notion target) {
		List<SimpleMatch> smatches = new ArrayList<SimpleMatch>();
		for (Match match : matches) {
			if (match instanceof SimpleMatch && ((SimpleMatch) match).getTarget().equals(target)) {
				smatches.add((SimpleMatch) match);
			}
		}
		return smatches;
	}

	/** Returns the unique simple matches in the Mapping with the same source and target. */
	public SimpleMatch getSimpleMatch(Element source, Notion target) {
		for (Match match : matches) {
			if (match instanceof SimpleMatch && match.getSource().equals(source) && ((SimpleMatch) match).getTarget().equals(target)) {
				return (SimpleMatch) match;
			}
		}
		return null;
	}

	/** Returns the single composite match, in this mapping, with the given source. */
	public CompositeMatch getCompositeMatchBySource(Element elem) {
		for (Match match : matches) {
			if (match instanceof CompositeMatch && match.getSource().equals(elem)) {
				return (CompositeMatch) match;
			}
		}
		return null;
	}

	// /** Returns the single composite match, in this mapping, for the simple match. */
	// public CompositeMatch getCompositeMatchByComponent(SimpleMatch match) {
	// for (CompositeMatch cmatch : getCompositeMatches()) {
	// for (SimpleMatch smatch : cmatch.getComponents()) {
	// // is the simple match part of a composite match?
	// if (match.equals(smatch)) {
	// return cmatch;
	// }
	// }
	// }
	// return null;
	// }

	/** Returns the matchs of this mapping with the given UFOType . */
	public List<Match> getMatchesBySourceUfotype(UFOType type) {
		List<Match> tmatches = new ArrayList<Match>();
		for (Match match : matches) {
			if (match.getSource().getIndirectUfotype() == type) {
				tmatches.add(match);
			}
		}
		return tmatches;
	}

	/** Returns the Current Coverage Situation of an Element in the context of THIS MAPPING. */
	public CoverateSituation getCoverageSituation(Element elem) {
		CoverateSituation situation = CoverateSituation.NONCOVERED;
		for (Match match : matches) {
			if (match.getSource().equals(elem)) {
				Coverage cover = match.getCoverage();
				if (cover == Coverage.EQUIVALENT || cover == Coverage.PARTIAL) {
					situation = CoverateSituation.FULLY;
					break;
				} else if (cover == Coverage.WIDER || cover == Coverage.INTERSECTION) {
					situation = CoverateSituation.PARTIALLY;
				}
			}
		}
		return situation;
	}

	/** Returns the current non covered elements of the mapping. */
	public List<Element> getNonCoveredElements() {
		List<Element> elems = new LinkedList<Element>(base.getElements());
		for (Match match : matches) {
			elems.remove(match.getSource());
		}
		return elems;
	}

	/** Returns the current partially covered elements of the mapping. */
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

	/** Returns the current fully covered elements of the mapping. */
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

	/** Returns the current non/partially covered elements of the given type in the mapping. */
	public List<Element> getNonFullyCoveredElementsByUfotype(UFOType type) {
		List<Element> elems = new LinkedList<Element>(base.getElementsByUfotype(type));
		for (Match match : matches) {
			Coverage cover = match.getCoverage();
			// removing elements with Equivalent or Partial, remmaining the ones with ONLY Wider/Intersection coverage.
			if (cover == Coverage.EQUIVALENT || cover == Coverage.PARTIAL) {
				elems.remove(match.getSource());
			}
		}
		return elems;
	}

	/** Adds a match to the mapping. */
	public void addMatch(Match match) {
		// Adds a simple match to the mapping.
		if (match instanceof SimpleMatch) {
			// If there's no simple match with the same source and target in the mapping
			if (getSimpleMatch(match.getSource(), ((SimpleMatch) match).getTarget()) == null) {
				this.matches.add(match);
			} else {
				return;
			}
		} else if (match instanceof CompositeMatch) {
			// Adds a composite match to the mapping, replacing the previous with the same source.
			Match previous = getCompositeMatchBySource(match.getSource());
			matches.remove(previous);
			matches.add(match);
		}
		match.setMapping(this);

		// Starting the mapping, if first Match
		if (status == MappingStatus.PLANNED) {
			status = MappingStatus.STARTED;
		}
	}

	/** Removes a Match from the Mapping. */
	public void removeMatch(Match rmatch) {
		matches.remove(rmatch);
	}

	/** Identifies and returns the conflicts related only to this mapping. */
	public List<Conflict> identifyConflicts() {
		List<Conflict> conflicts = new ArrayList<Conflict>();
		// for each target concept, get all matches
		for (Notion target : (List<Notion>) getTarget().getNotions()) {
			List<SimpleMatch> tmatches = getSimpleMatchesByTarget(target);
			// analyses each pair of matches for conflicts (T2)
			for (int i = 0; i < tmatches.size(); i++) {
				for (int j = i + 1; j < tmatches.size(); j++) {
					Conflict conflict = getConflict(tmatches.get(i), tmatches.get(j));
					if (conflict != null)
						conflicts.add(conflict);
				}
			}
		}
		return conflicts;
	}

	/** Returns the conflict between two matches with 'distinct' sources and same target (T2), if exists. */
	private Conflict getConflict(Match match1, Match match2) {
		Coverage E = Coverage.EQUIVALENT, P = Coverage.PARTIAL, W = Coverage.WIDER, I = Coverage.INTERSECTION;
		Coverage cover1 = match1.getCoverage(), cover2 = match2.getCoverage();
		// PP, PI, IP, II: no conflict
		if ((cover1 == P || cover1 == I) && (cover2 == P || cover2 == I)) {
			return null;
		}
		// EE: same as conflict
		if (cover1 == E && cover2 == E) {
			if (!match1.getSource().sameAs(match2.getSource()))
				return new Conflict(match1, match2, RelationType.SAMEAS);

			// EI, WI, IE, IW, WW: interserction conflict
		} else if (cover1 == I || cover2 == I || (cover1 == W && cover2 == W)) {
			if (!match1.getSource().intersects(match2.getSource()))
				return new Conflict(match1, match2, RelationType.INTERSECTION);

			// (EP, WE, WP) and (no wholeof): whole of conflict
		} else if ((cover1 == E && cover2 == P) || (cover1 == W && (cover2 == E || cover2 == P))) {
			if (!match2.getSource().isIndirectPartOf(match1.getSource()))
				return new Conflict(match2, match1, RelationType.PARTOF);

			// (EW, PE, PW) and (no partof): part of conflict
		} else if ((cover1 == E && cover2 == W) || (cover1 == P && (cover2 == E || cover2 == W))) {
			if (!match1.getSource().isIndirectPartOf(match2.getSource()))
				return new Conflict(match1, match2, RelationType.PARTOF);
		}
		return null; // Other: no conclusion
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
		return getBase() + " \u21e8 " + getTarget();
	}

}