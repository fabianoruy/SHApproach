package shmapper.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import shmapper.model.Element.CoverageSituation;
import shmapper.model.Match.Coverage;
import shmapper.model.Notion.UFOType;
import shmapper.model.Relation.RelationType;

/** Represents an abstract Mapping between a Model and an Ontology (vertical) or another Model (horizontal). */
public abstract class Mapping extends SerializableObject {
	private static final long	serialVersionUID	= 1581632337297022767L;
	private StandardModel		base;
	private SHInitiative		initiative;
	private boolean				structural;
	private String				analysis;
	private List<Match>			matches;
	private MappingStatus		status;

	public static enum MappingStatus {
		PLANNED, STARTED, FINISHED
	}

	public Mapping(StandardModel base, SHInitiative initiative) {
		this.base = base;
		this.initiative = initiative;
		this.status = MappingStatus.PLANNED;
		this.matches = new ArrayList<Match>();
	}

	public StandardModel getBase() {
		return base;
	}

	public SHInitiative getInitiative() {
		return initiative;
	}

	public abstract Package getTarget();

	public boolean isStructural() {
		return structural;
	}

	public void setStructural(boolean structural) {
		this.structural = structural;
	}

	/** Returns the coverage of a single element. */
	protected double getCoverage(Element element) {
//		double cover = 0.0;
		List<Match> matches = getMatchesBySource(element);
		return getCoverage(matches);
//		for (Match match : matches) {
//			if (match.getCoverage() == Coverage.FULL)
//				return Coverage.FULL.getValue();
//			cover += match.getCoverage().getValue();
//		}
//		if (cover >= Coverage.FULL.getValue())
//			cover = ((double) matches.size()) / (matches.size() + 1);
//		return cover;
	}

	/** Returns the coverage of the matches over the Standard's Elements (base). */
	public int getCoverage() {
		// System.out.println("\n" + this + " COVERAGE");
		// coverage (%): SOMA(baseModel.elements.coverage) / baseModel.elements;
		int coverage;
		List<Element> elements = new ArrayList<>(getBase().getElements());
		elements.removeAll(getDiscardedElements());

		int total = elements.size();
		double sum = 0.0;
		for (Element elem : elements) {
			sum += getCoverage(elem);
		}
		coverage = (int) Math.round(sum * 100 / total);
		// System.out.println("COVERAGE: (" + sum + "/" + total + ") = " + coverage + "\n");
		if (coverage == 100) {
			this.status = MappingStatus.FINISHED;
		} else if (coverage > 0) {
			this.status = MappingStatus.STARTED;
		} else {
			this.status = MappingStatus.PLANNED;
		}
		return coverage;
	}
	
	/** Returns the coverage based on a list of matches of the same element. */
	protected double getCoverage(List<Match> matches) {
		double cover = 0.0;
		for (Match match : matches) {
			// TODO: exclude!
//			if (match.getMatchType() == MatchType.EQUIVALENT || match.getMatchType() == MatchType.PARTIAL)
//				match.setCoverage(Coverage.FULL);

			if (match.getCoverage() == Coverage.FULL)
				return Coverage.FULL.getValue(); // 1.0
			cover += match.getCoverage().getValue();
		}
		if (cover >= Coverage.FULL.getValue())
			cover = ((double) matches.size()) / (matches.size() + 1); // < 1.0
		return cover;
	}


	/** Returns the coverage of the matches over the Standard's Elements (base). */
	// public int getCoverage0() {
	// // coverage (%): baseModel.elements.([E] + [P] + [W]/2 + [O]/2) / baseModel.elements;
	// int all = getBase().getElements().size() - getDiscardedElements().size();
	// int partially = getPartiallyCoveredElements().size();
	// int noncovered = getNonCoveredElements().size();
	// int fully = all - partially - noncovered;
	// double coverage = ((partially * 0.35 + fully) / all) * 100;
	// if (fully == all) {
	// this.status = MappingStatus.FINISHED;
	// return 100;
	// } else if (coverage > 0.1) {
	// this.status = MappingStatus.STARTED;
	// } else {
	// this.status = MappingStatus.PLANNED;
	// }
	// // System.out.println(this + ": All(" + all + "), Full(" + fully + "), Part(" + partially + "), Non("
	// // +noncovered + "): Cover(" + coverage + "%)");
	// return (int) Math.round(coverage);
	// }

	public MappingStatus getStatus() {
		return status;
	}

	public List<Match> getMatches() {
		return new ArrayList<>(matches);
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

	/** Returns a map of the current situations of all elements of this mapping. */
	public Map<Element, Element.CoverageSituation> getElementsSituations() {
		Map<Element, Element.CoverageSituation> situations = new HashMap<>();
		for (Element elem : base.getElements()) {
			situations.put(elem, getCoverageSituation(elem));
		}
		return situations;
	}

	/** Returns the Current Coverage Situation of an Element in the context of THIS MAPPING. */
	public CoverageSituation getCoverageSituation(Element elem) {
		if (initiative.isDiscarded(elem))
			return CoverageSituation.DISCARDED;
		List<Match> matches = getMatchesBySource(elem);
		if (matches.isEmpty())
			return CoverageSituation.NONCOVERED;
		for (Match match : matches) {
			if (match.getCoverage() == Coverage.FULL) {
				return CoverageSituation.FULLY;
			}
		}
		return CoverageSituation.PARTIALLY;
	}

	/** Returns the current discared elements of the mapping. */
	public List<Element> getDiscardedElements() {
		List<Element> elems = new ArrayList<Element>();
		for (Element elem : initiative.getDiscardedElements()) {
			if (base.getElements().contains(elem)) {
				elems.add(elem);
			}
		}
		return elems;
	}

	/** Returns the current non covered elements of the mapping. */
	public List<Element> getNonCoveredElements() {
		List<Element> elems = new LinkedList<Element>(base.getElements());
		for (Match match : matches) {
			elems.remove(match.getSource());
		}
		elems.removeAll(getDiscardedElements());
		return elems;
	}

	/** Returns the current fully covered elements of the mapping. */
	public List<Element> getFullyCoveredElements() {
		List<Element> elems = new ArrayList<Element>();
		for (Match match : matches) {
			if (match.getCoverage() == Coverage.FULL)
				elems.add(match.getSource());
		}
		return elems;
	}

	/** Returns the current partially covered elements of the mapping. */
	public List<Element> getPartiallyCoveredElements() {
		List<Element> elems = new LinkedList<Element>(base.getElements());
		// removing discarded, non covered and fully covered elements
		elems.removeAll(getDiscardedElements());
		elems.removeAll(getNonCoveredElements());
		elems.removeAll(getFullyCoveredElements());
		return elems;
	}

	/** Returns the current non/partially covered elements of the given type in the mapping. */
	// public List<Element> getNonFullyCoveredElementsByUfotype(UFOType type) {
	// List<Element> elems = new LinkedList<Element>(base.getElementsByUfotype(type));
	// for (Match match : matches) {
	// MatchType cover = match.getMatchType();
	// // removing elements with Equivalent or Partial (fully covered).
	// if (cover == MatchType.EQUIVALENT || cover == MatchType.PARTIAL) {
	// elems.remove(match.getSource());
	// }
	// }
	// return elems;
	// }

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

	public String getAnalysis() {
		return analysis;
	}

	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}

	/** Identifies and returns the Issues related only to this mapping. */
	public List<Issue> identifyIssues() {
		List<Issue> issues = new ArrayList<Issue>();
		// for each target concept, get all matches
		for (Notion target : (List<Notion>) getTarget().getNotions()) {
			List<SimpleMatch> tmatches = getSimpleMatchesByTarget(target);
			// analyses each pair of matches for questions (T2)
			for (int i = 0; i < tmatches.size(); i++) {
				for (int j = i + 1; j < tmatches.size(); j++) {
					Issue issue = getIssue(tmatches.get(i), tmatches.get(j));
					if (issue != null)
						issues.add(issue);
				}
			}
		}
		return issues;
	}

	/** Returns the Issue between two matches with 'distinct' sources and same target (T2), if it exists. */
	private Issue getIssue(Match match1, Match match2) {
		MatchType E = MatchType.EQUIVALENT, P = MatchType.PARTIAL, W = MatchType.WIDER, O = MatchType.OVERLAP;
		MatchType cover1 = match1.getMatchType(), cover2 = match2.getMatchType();
		// PP, PI, IP, II: no issue
		if ((cover1 == P || cover1 == O) && (cover2 == P || cover2 == O)) {
			return null;
		}
		// EE: equivalent issue
		if (cover1 == E && cover2 == E) {
			if (!match1.getSource().isEquivalentTo(match2.getSource()))
				return new Issue(match1, match2, RelationType.EQUIVALENT);

			// EO, WO, OE, OW, WW: overlap issue
		} else if (cover1 == O || cover2 == O || (cover1 == W && cover2 == W)) {
			if (!match1.getSource().intersectsWith(match2.getSource()))
				return new Issue(match1, match2, RelationType.INTERSECTION);

			// (EP, WE, WP) and (no wholeof): whole of issue
		} else if ((cover1 == E && cover2 == P) || (cover1 == W && (cover2 == E || cover2 == P))) {
			if (!match2.getSource().isIndirectPartOf(match1.getSource()))
				return new Issue(match2, match1, RelationType.PARTOF);

			// (EW, PE, PW) and (no partof): part of issue
		} else if ((cover1 == E && cover2 == W) || (cover1 == P && (cover2 == E || cover2 == W))) {
			if (!match1.getSource().isIndirectPartOf(match2.getSource()))
				return new Issue(match1, match2, RelationType.PARTOF);
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