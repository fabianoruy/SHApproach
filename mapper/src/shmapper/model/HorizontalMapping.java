package shmapper.model;

/* Represents a Horizontal Mapping between two Models. */
public class HorizontalMapping extends Mapping {
	private static final long	serialVersionUID	= -1747666812598375016L;
	private StandardModel		target;

	public HorizontalMapping(StandardModel base, StandardModel target) {
		super(base);
		this.target = target;
	}

	@Override
	public StandardModel getTarget() {
		return target;
	}

}