package shmapper.model;

import com.change_vision.jude.api.inf.model.IClass;

/* Represents an Element from a Standard Model. */
public class Element extends Notion {
	private static final long	serialVersionUID	= -3365856985890766861L;
	private Model				standard;

	public static enum CoverateSituation {
		NONCOVERED, PARTIALLY, FULLY, DISCARDED;
	}

	public Element(Model standard, IClass astahClass) {
		super(astahClass);
		this.standard = standard;
	}

	public Model getModel() {
		return standard;
	}

	public boolean isIntegrated() {
		return (standard == null);
	}

	@Override
	public boolean isBasetype() {
		return (standard.isStructural());
	}

	@Override
	public Package getPackage() {
		return standard;
	}

}