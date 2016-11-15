package shmapper.model;

/* Represents a Vertical Mapping between a Model and an Ontology. */
public class VerticalMapping extends Mapping {
	private SeonView target;

	public VerticalMapping(StandardModel base, SeonView target) {
		super(base);
		this.target = target;
	}

	@Override
	public SeonView getTarget() {
		return target;
	}

	@Override
	public int getCoverage() {
		// coverage (%): baseModel.elements.([E] + [P] + [W]/2 + [I]/2) / baseModel.elements;
		int all = getBase().getElements().size();
		int partially = getPartiallyCoveredElements().size();
		int noncovered = getNonCoveredElements().size();
		int fully = all - partially - noncovered;
		double coverage = ((partially / 2.0 + fully) / all) * 100;
		System.out.println("All(" + all + "), Full(" + fully + "), Part(" + partially + "), Non(" + noncovered + "): Cover(" + coverage + "%)");
		return (int) Math.round(coverage);
	}

}