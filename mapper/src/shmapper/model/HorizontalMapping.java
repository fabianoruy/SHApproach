package shmapper.model;

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
		// TODO
		return 0;
	}

	public HorizontalMapping getMirror() {
		// TODO: mirror mapping should be created in the begining (together with the Content Mappings)
		if(mirror == null) {
			this.mirror = new HorizontalMapping(target, super.getBase());
		}
		return mirror;
	}

//	public void setMirror(HorizontalMapping mirror) {
//		this.mirror = mirror;
//	}

}