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
	
	@Override
	public int getCoverage() {
		//TODO: coverage (%): baseModel.elements / baseModel.elements.([E] + [P] + [W]/2 + [I]/2);
		return 80;
	}

	@Override
	public String toString() {
		return getBases() + " <--> " + getTarget();
	}

}