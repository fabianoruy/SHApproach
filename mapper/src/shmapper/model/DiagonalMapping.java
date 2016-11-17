package shmapper.model;

import java.util.List;

/* Represents a "Diagonal" Mapping between a number of Standards Models and a Integrated CM. */
public class DiagonalMapping extends Mapping {
	private List<StandardModel>	bases;
	private IntegratedModel		target;

	public DiagonalMapping(List<StandardModel> bases, IntegratedModel target) {
		super(null);
		this.target = target;
		this.bases = bases;
	}

	public List<StandardModel> getBases() {
		return bases;
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


	@Override
	public String toString() {
		return getBases() + " <--> " + getTarget();
	}

}