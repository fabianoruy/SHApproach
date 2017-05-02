package shmapper.model;

import java.util.List;

import shmapper.model.Match.Coverage;

/* Represents a "Diagonal" Mapping between a number of Standards Models and a Integrated CM. */
public class DiagonalMapping extends Mapping {
	private static final long	serialVersionUID	= -5114513298327757457L;
	private IntegratedModel		target;

	public DiagonalMapping(StandardModel base, IntegratedModel target, SHInitiative initiative) {
		super(base, initiative);
		this.target = target;
	}

	/** Returns the coverage of a single element. */
	public double getCoverage(Element element) {
		double cover = 0.0;
		// if it has a decision, coverage is full
		if (getInitiative().getDecision(element) != null) {
			//return Coverage.FULL.getValue();
		}
		List<Match> matches = getMatchesBySource(element);
		matches.addAll(getInitiative().getVerticalContentMapping(getBase()).getMatchesBySource(element));
		for (Match match : matches) {
			if (match.getCoverage() == Coverage.FULL)
				return Coverage.FULL.getValue();
			cover += match.getCoverage().getValue();
		}
		if (cover >= Coverage.FULL.getValue())
			cover = ((double) matches.size()) / (matches.size() + 1);
		return cover;
	}

	/** Returns the coverage of the matches over the Standard's Elements (base). */
	public int getCoverage() {
		return super.getCoverage() - getInitiative().getVerticalContentMapping(getBase()).getCoverage();
	}

	@Override
	public IntegratedModel getTarget() {
		return target;
	}

}