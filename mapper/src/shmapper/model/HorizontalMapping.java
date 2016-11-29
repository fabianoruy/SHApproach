package shmapper.model;

import java.util.List;

import shmapper.applications.MappingApp.QuestionType;

/* Represents a Horizontal Mapping between two Models. */
public class HorizontalMapping extends Mapping {
	private static final long	serialVersionUID	= -1747666812598375016L;
	private StandardModel		target;
	private HorizontalMapping	mirror;

	public HorizontalMapping(StandardModel base, StandardModel target) {
		super(base);
		this.target = target;
	}

	@Override
	public StandardModel getTarget() {
		return target;
	}

	/* Returns the coverage of the matchs over the Standard's Elements (target). */
	public int getTargetCoverage() {
		return getMirror().getCoverage();
	}

	public HorizontalMapping getMirror() {
		// TODO: mirror mapping should be created in the begining (together with the Content Mappings)
		if (mirror == null) {
			this.mirror = new HorizontalMapping(target, super.getBase());
		}
		// System.out.println("Mirror: "+ mirror);
		return mirror;
	}

	// public void setMirror(HorizontalMapping mirror) {
	// this.mirror = mirror;
	// }

	/** Checks if a given partially covered element can lead to a composite match. */
	public boolean checkCompositeChance(Element source) {
		// Checks if the element has a set of only partial coverages ([W] or [I]) possibly leading to a Composite Match.
		for (Match match : this.getMatchesBySource(source)) {
			Coverage cover = match.getCoverage();
			if (cover == Coverage.EQUIVALENT || cover == Coverage.PARTIAL)
				return false;
		}
		if (this.getSimpleMatchesBySource(source).size() > 1)
			return true;
		return false;
	}

}