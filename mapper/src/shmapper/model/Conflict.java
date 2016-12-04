package shmapper.model;

import java.util.Arrays;
import java.util.List;

import shmapper.model.Relation.RelationType;

/** Represents a conflict between two or more matches. */
public class Conflict {
	private Match		match1;
	private Match		match2;
	private String		description;
	private Relation	relation;
	private boolean		solved;

	public Conflict(Match match1, Match match2, RelationType type) {
		this.match1 = match1;
		this.match2 = match2;
		this.relation = new Relation(match1.getSource(), match2.getSource(), type);
		this.solved = false;
	}

	public List<Match> getMatches() {
		return Arrays.asList(match1, match2);
	}

	public String getDescription() {
		return description;
	}

	public boolean isSolved() {
		return solved;
	}

	public void solve() {
		relation.getSource().addRelation(relation);
		relation.getTarget().addRelation(relation);
		this.solved = true;
	}

	@Override
	public String toString() {
		//return "(" + match1 + ") & (" + match2 + "): Is " + relation + " ?";
		return "M1: " + match1 + "\nM2: " + match2 + "\nIs " + relation + " ?\n";
	}

}