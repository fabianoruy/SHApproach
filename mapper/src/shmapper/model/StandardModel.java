package shmapper.model;

import com.change_vision.jude.api.inf.model.IPackage;

/* Represents a Model extracted from a Standard. */
public class StandardModel extends Model {
	private static final long	serialVersionUID	= -669968173376332948L;
	private String				scope;

	public StandardModel(boolean struct, IPackage astahPack) {
		super(struct, astahPack);
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

}