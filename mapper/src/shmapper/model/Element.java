package shmapper.model;

import com.change_vision.jude.api.inf.model.IClass;

/* Represents an Element from a Standard Model. */
public class Element extends Notion {
	private Model standard;

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