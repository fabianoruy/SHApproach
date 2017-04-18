package shmapper.model;

import java.util.ArrayList;
import java.util.List;

/* Represents a Horizontal Mapping between two Models. */
public class HorizontalMapping extends Mapping {
	private static final long	serialVersionUID	= -1747666812598375016L;
	private StandardModel		target;
	private HorizontalMapping	mirror;
	private boolean				deduced;

	public HorizontalMapping(StandardModel base, StandardModel target, SHInitiative initiative) {
		super(base, initiative);
		this.target = target;
		this.deduced = false;
	}

	@Override
	public StandardModel getTarget() {
		return target;
	}

	/** Returns the coverage of the matchs over the Standard's Elements (target). */
	public int getTargetCoverage() {
		return getMirror().getCoverage();
	}

	public HorizontalMapping getMirror() {
		// TODO: mirror mapping should be created in the begining (together with the Content Mappings)
		if (mirror == null) {
			this.mirror = new HorizontalMapping(target, super.getBase(), getInitiative());
			this.mirror.mirror = this;
		}
		// System.out.println("Mirror: "+ mirror);
		return mirror;
	}

	/** Checks if a given partially covered element is able to create a composite match. */
	public boolean isCompositeAble(Element source) {
		// Checks if the element has a set of only partial coverages ([W] or [O]) possibly leading to a Composite Match.
		List<SimpleMatch> smatches = new ArrayList<>();
		for (SimpleMatch match : getSimpleMatchesBySource(source)) {
			if(match.getMatchType() == MatchType.WIDER || match.getMatchType() == MatchType.OVERLAP)
				smatches.add(match);
		}
		return (smatches.size() >= 2);
	}

	public boolean isDeduced() {
		return deduced;
	}

	public void setDeduced(boolean deduced) {
		this.deduced = deduced;
	}

}