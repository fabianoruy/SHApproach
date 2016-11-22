package shmapper.model;

/* Represents a "Diagonal" Mapping between a number of Standards Models and a Integrated CM. */
public class DiagonalMapping extends Mapping {
	private static final long	serialVersionUID	= -5114513298327757457L;
	private IntegratedModel		target;

	public DiagonalMapping(StandardModel base, IntegratedModel target) {
		super(base);
		this.target = target;
	}

	@Override
	public IntegratedModel getTarget() {
		return target;
	}

	/* Returns the coverage of the matchs over the base Elements. */
	@Override
	public int getCoverage() {
		return 0;
	}

}