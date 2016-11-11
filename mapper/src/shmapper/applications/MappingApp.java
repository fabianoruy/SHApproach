package shmapper.applications;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;

import shmapper.model.Concept;
import shmapper.model.Coverage;
import shmapper.model.Diagram;
import shmapper.model.Element;
import shmapper.model.Notion;
import shmapper.model.SHInitiative;
import shmapper.model.SimpleMatch;

/** Responsible for providing the services for the mapping tasks. */
public class MappingApp {
    private SHInitiative initiative;
    private List<SimpleMatch> currentMatches = new ArrayList<SimpleMatch>(); //TODO: get from the initiative mapping
    private String message;

    public MappingApp(SHInitiative initiative) {
	this.initiative = initiative;
    }

    public List<SimpleMatch> getCurrentMatches() {
	return currentMatches;
    }

    public String getMessage() {
	return message;
    }

    /* Creates a hash containg all the diagram notions (as keys) and their respective coords in the diagram. */
    public Map<Notion, String> createNotionsCoordsHash(Diagram diagram) {
	Map<Notion, String> coordsHash = new HashMap<Notion, String>();
	// Getting each Notion (Class) in the diagram and its position.
	try {
	    for (IPresentation present : diagram.getAstahDiagram().getPresentations()) {
		if (present instanceof INodePresentation && present.getType().equals("Class")) {
		    INodePresentation node = (INodePresentation) present;
		    Notion notion = getNotionById(((IClass) node.getModel()).getId());
		    coordsHash.put(notion, getMapCoords(node, diagram.getAstahDiagram().getBoundRect()));
		}
	    }
	} catch (InvalidUsingException e) {
	    e.printStackTrace();
	}
	return coordsHash;
    }

    /* Returns a Notion by the id. */
    private Notion getNotionById(String id) {
	//TODO: doesn't need to search from all the initiative notions.
	for (Notion notion : initiative.getAllNotions()) {
	    if (notion.getId().equals(id))
		return notion;
	}
	return null;
    }

    /* Returns the String Coords of a html image MAP diagram. */
    private String getMapCoords(INodePresentation node, Rectangle2D adjust) {
	int x = (int) Math.round(node.getLocation().getX() - adjust.getX());
	int y = (int) Math.round(node.getLocation().getY() - adjust.getY());
	int w = (int) Math.round(node.getWidth());
	int h = (int) Math.round(node.getHeight());
	return "" + x + "," + y + "," + (x + w) + "," + (y + h);
    }

    /* Creates a new (simple) Match. */
    public SimpleMatch createMatch(String elemId, String concId, String coverName, String comm) {
	Element elem = (Element) initiative.getNotionById(elemId);
	Concept conc = (Concept) initiative.getNotionById(concId);
	Coverage cover = Coverage.valueOf(coverName);

	SimpleMatch match = new SimpleMatch(elem, conc, cover, comm);
	if (!validateOntologyDisjointness(match)) {
	    return null;
	}

	currentMatches.add(match);
	System.out.println("(" + currentMatches.size() + ") " + match);
	message = "Match " + match + " created!";
	return match;
    }

    /* Validates the Ontology Disjointness (T1). */
    private boolean validateOntologyDisjointness(SimpleMatch match) {
	Element elem = match.getSource();
	message = "";
	for (SimpleMatch otherMatch : currentMatches) {
	    Element otherElem = otherMatch.getSource();
	    if (otherElem.equals(elem)) {
		message += "The element " + elem + " is already matched with other concept (" + otherMatch + ").\n";
		Coverage cover = match.getCoverage();
		Coverage othercover = otherMatch.getCoverage();
		// both coverages must be [W] or [I]
		if ((cover == Coverage.WIDER || cover == Coverage.INTERSECTION)
			&& (othercover == Coverage.WIDER || othercover == Coverage.INTERSECTION)) {
		    message += " Do these matches together fully cover the element " + elem + "?\n";
		} else {
		    message += " Multiple matches for the same element are allowed only for WIDER and INTERSECTION coverages.\n";
		    return false;
		}
		message += "\n";
	    }
	}
	return true;
    }

}
