package shmapper.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import shmapper.model.Ontology.Level;
import shmapper.model.Relation.RelationType;

/* Represents a Vertical Mapping between a Model and an Ontology. */
public class VerticalMapping extends Mapping {
	private static final long	serialVersionUID	= -7873764022001248669L;
	private SeonView			target;

	public VerticalMapping(StandardModel base, SeonView target, SHInitiative initiative) {
		super(base, initiative);
		this.target = target;
	}

	@Override
	public SeonView getTarget() {
		return target;
	}

	/** Identifies and returns the Relational Issues related only to this mapping (only for Vertical). */
	public List<RelationalIssue> identifyRelationalIssues() {
		List<RelationalIssue> issues = new ArrayList<RelationalIssue>();
		Set<Relation> relations = new HashSet<Relation>();
		// select all part-whole relations from the target domain ontologies.
		for (Ontology ontology : getTarget().getOntologies()) {
			if (ontology.getLevel() == Level.DOMAIN) {
				for (Concept concept : ontology.getConcepts()) {
					for (Relation relation : concept.getRelations()) {
						if (relation.getType() == RelationType.PARTOF) {
							relations.add(relation);
							// System.out.println("(" + relations.size() + ") " + relation);
						}
					}
				}
			}
		}
		// for each relation
		for (Relation relation : relations) {
			// get the matches of the whole
			List<SimpleMatch> wholeMatches = getSimpleMatchesByTarget(relation.getSource());
			// get the matches of the part
			List<SimpleMatch> partMatches = getSimpleMatchesByTarget(relation.getTarget());
			// test the issue for each combination
			for (SimpleMatch wmatch : wholeMatches) {
				for (SimpleMatch pmatch : partMatches) {
					RelationalIssue issue = getRelationalIssue(wmatch, pmatch, relation);
					if (issue != null) {
						issues.add(issue);
					}
				}
			}
		}
		return issues;
	}

	/** Returns the Issue between two matches of related concepts (T4), if it exists. */
	private RelationalIssue getRelationalIssue(Match match1, Match match2, Relation relation) {
		// Only for PART OF relations (could use other in the future)
		// relation.source == match1.target; relation.target == match2.target
		MatchType E = MatchType.EQUIVALENT, P = MatchType.PARTIAL, W = MatchType.WIDER;
		MatchType cover1 = match1.getMatchType(), cover2 = match2.getMatchType();
		// EE, EP, WE, WP: Part of relational issue
		if ((cover1 == E || cover1 == W) && (cover2 == E || cover2 == P)) {
			if (!match2.getSource().isIndirectPartOf(match1.getSource())) {
				// verifying if the element whole has an issue with the same ontology relation
				Element welem = (Element) match2.getSource().getWhole();
				if (welem != null) {
					Match wmatch2 = getSimpleMatch(welem, ((SimpleMatch) match2).getTarget());
					if (wmatch2 != null && getRelationalIssue(wmatch2, match1, relation) != null)
						return null;
				}
				return new RelationalIssue(match2, match1, relation, RelationType.PARTOF);
			}
		}
		return null; // Other: no conclusion
	}

}