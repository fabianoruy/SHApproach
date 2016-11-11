package shmapper.model;

import java.util.List;

/* Represents a composition of Matches for the same source. */
public class CompositeMatch extends Match {
    private List<Match> matches;

    public CompositeMatch(Element source, Coverage cover, String comm, List<Match> matches) {
	super(source, cover, comm);
	this.matches = matches;
    }

    public List<Match> getMatches() {
	return matches;
    }

}
