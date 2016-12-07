package shmapper.model;

import java.util.Arrays;
import java.util.List;

import shmapper.model.Relation.RelationType;

/** Represents a Issue involving two matches in the same mapping. */
public class Issue {
	protected Match		match1;
	protected Match		match2;
	protected Relation	solution;
	private String		comment;
	private boolean		solved;

	public Issue(Match match1, Match match2, RelationType type) {
		this.match1 = match1;
		this.match2 = match2;
		this.solution = new Relation(match1.getSource(), match2.getSource(), type);
		this.solved = false;
	}

	public List<Match> getMatches() {
		return Arrays.asList(match1, match2);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isSolved() {
		return solved;
	}

	public void solve() {
		solution.getSource().addRelation(solution);
		solution.getTarget().addRelation(solution);
		this.solved = true;
	}

	@Override
	public String toString() {
		// return "(" + match1 + ") & (" + match2 + "): Is " + solution + " ?";
		return "M1: " + match1 + "\nM2: " + match2 + "\nIs " + solution + "?\n";
	}

}