package shmapper.model;

import java.util.ArrayList;
import java.util.List;

import shmapper.model.Mapping.MappingStatus;
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
	protected double getCoverage(Element element) {
		// double cover = 0.0;
		// if it has a decision, coverage is full
		// if (getInitiative().getDecision(element) != null) {
		// return Coverage.FULL.getValue();
		// }
		List<Match> matches = getMatchesBySource(element);
		matches.addAll(getInitiative().getVerticalContentMapping(getBase()).getMatchesBySource(element));
		return getCoverage(matches);
		// for (Match match : matches) {
		// if (match.getCoverage() == Coverage.FULL)
		// return Coverage.FULL.getValue();
		// cover += match.getCoverage().getValue();
		// }
		// if (cover >= Coverage.FULL.getValue())
		// cover = ((double) matches.size()) / (matches.size() + 1);
		// return cover;
	}
	
	/** Returns the coverage of the matches over the Standard's Elements (base). */
	public int getCoverage() {
		return super.getCoverage() - getInitiative().getVerticalContentMapping(getBase()).getCoverage();
	}

	/** Returns the coverage of a single element. */
	protected double getTotalCoverage(Element element) {
		// if it has a decision, coverage is full
		if (getInitiative().getDecision(element) != null) {
			return Coverage.FULL.getValue();
		}
		List<Match> matches = getMatchesBySource(element);
		matches.addAll(getInitiative().getVerticalContentMapping(getBase()).getMatchesBySource(element));
		return getCoverage(matches);
	}

	/** Returns the coverage of all matches and decisions over the Standard's Elements (base). */
	public int getDecisionsCoverage() {
		int coverage;
		List<Element> elements = new ArrayList<>(getBase().getElements());
		elements.removeAll(getDiscardedElements());
		double sum = 0.0;
		for (Element elem : elements) {
			sum += getTotalCoverage(elem);
		}
		coverage = (int) Math.round(sum * 100 / elements.size());
		// System.out.println("COVERAGE: (" + sum + "/" + total + ") = " + coverage + "\n");
		return coverage - super.getCoverage();
	}

	@Override
	public IntegratedModel getTarget() {
		return target;
	}

}