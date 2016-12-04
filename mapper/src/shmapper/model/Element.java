package shmapper.model;

import com.change_vision.jude.api.inf.model.IClass;

/* Represents an Element from a Standard Model. */
public class Element extends Notion {
	private static final long	serialVersionUID	= -3365856985890766861L;
	private Model				model;

	public static enum CoverateSituation {
		NONCOVERED, PARTIALLY, FULLY, DISCARDED;
	}

	/* General constructor. */
	public Element(Model model, IClass astahClass) {
		super(astahClass);
		this.model = model;
	}

	/* Constructor for new ICM Elements. */
	public Element(String name, String def, Notion type, IntegratedModel model) {
		super(name, def, type);
		this.model = model;
	}

	public Model getModel() {
		return model;
	}

	public boolean isIntegrated() {
		return (model == null);
	}

	@Override
	public boolean isBasetype() {
		return (model.isStructural());
	}

	@Override
	public Model getPackage() {
		return model;
	}

}