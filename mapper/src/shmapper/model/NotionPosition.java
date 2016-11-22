package shmapper.model;

import java.awt.geom.Rectangle2D;

import com.change_vision.jude.api.inf.presentation.INodePresentation;

/* Represents the position of a Notion in a Diagram. */
public class NotionPosition extends SerializableObject {
	private static final long	serialVersionUID	= -1833025877741195773L;
	private int					xPos;
	private int					yPos;
	private int					height;
	private int					width;
	private Notion				notion;

	public NotionPosition(Notion notion, INodePresentation node, Rectangle2D adjust) {
		this.notion = notion;
		xPos = (int) Math.round(node.getLocation().getX() - adjust.getX());
		yPos = (int) Math.round(node.getLocation().getY() - adjust.getY());
		height = (int) Math.round(node.getHeight());
		width = (int) Math.round(node.getWidth());
	}

	/* Returns the string Coords of the notion in the Diagram. */
	public String getCoords() {
		return xPos + "," + yPos + "," + (xPos + width) + "," + (yPos + height);
	}

	public int getXpos() {
		return xPos;
	}

	public int getYpos() {
		return yPos;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public Notion getNotion() {
		return notion;
	}

	@Override
	public String toString() {
		return notion + " (" + getCoords() + ")";
	}
}