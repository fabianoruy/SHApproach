package shmapper.model;

import shmapper.model.Relation.RelationType;

/** Represents a Relational Issue involving two matches and a ontology relation in the same mapping. */
public class RelationalIssue extends Issue {
	private Relation relation;

	public RelationalIssue(Match match1, Match match2, Relation relation, RelationType type) {
		super(match1, match2, type);
		this.relation = relation;
	}

	public Relation getRelation() {
		return relation;
	}

	@Override
	public String toString() {
		// return "(" + match1 + ") & (" + match2 + "): Is " + solution + " ?";
		return "Relation: " + relation + "\nM1: " + match1 + "\nM2: " + match2 + "\nIs " + solution + "?\n";
	}

}