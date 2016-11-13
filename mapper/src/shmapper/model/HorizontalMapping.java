package shmapper.model;

/* Represents a Horizontal Mapping between two Models. */
public class HorizontalMapping extends Mapping {
	private Model target;

	public HorizontalMapping(StandardModel base, Model target) {
		super(base);
		this.target = target;
	}

	@Override
	public Model getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return "HM: " + getBase() + " x " + getTarget();
	}
	
}