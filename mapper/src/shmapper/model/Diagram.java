package shmapper.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IDiagram;

/* Represents a Diagram (for the Seon View, a Standard Content Model or the ICM). */
public class Diagram {
	private String					name;
	private String					description;
	private String					path;
	private String					width;
	private DiagramType				type;
	private List<NotionPosition>	positions;
	// private IDiagram astahDiagram;

	/* Seon View, Standard Structural Model, Integrated Structural Model, Standard Content Model, Integrated Content
	 * Model. */
	public static enum DiagramType {
		SEONVIEW, SSM, ISM, SCM, ICM
	}

	public Diagram(DiagramType type, IDiagram astahDiag) {
		this.name = astahDiag.getName();
		this.description = astahDiag.getDefinition();
		this.type = type;
		this.width = String.valueOf(astahDiag.getBoundRect().getWidth());
		this.positions = new ArrayList<NotionPosition>();
		// this.astahDiagram = astahDiag;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getWidth() {
		return width;
	}

	public DiagramType getType() {
		return type;
	}

	// public IDiagram getAstahDiagram() {
	// return astahDiagram;
	// }

	public List<NotionPosition> getPositions() {
		return positions;
	}

	public void addPosition(NotionPosition position) {
		this.positions.add(position);
	}

	@Override
	public String toString() {
		return this.name;
	}

}