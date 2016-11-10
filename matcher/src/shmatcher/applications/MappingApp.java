package shmatcher.applications;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;

import shmatcher.model.Diagram;
import shmatcher.model.Notion;
import shmatcher.model.SHInitiative;

/** Responsible for providing the services for the mapping tasks. */
public class MappingApp {
    private SHInitiative initiative;

    public MappingApp(SHInitiative initiative) {
	this.initiative = initiative;
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

    //    public JsonElement createJSON() {
    //	JsonObject root = new JsonObject();
    //	JsonArray elements = new JsonArray();
    //	JsonArray concepts = new JsonArray();
    //	int i = 0;
    //	for (Notion notion : initiative.getAllNotions()) {
    //	    JsonObject jnotion = new JsonObject();
    //	    jnotion.addProperty("id", "id"+i);
    //	    jnotion.addProperty("name", notion.getName());
    //	    jnotion.addProperty("definition", notion.getDefinition());
    //	    if (notion instanceof Element) {
    //		jnotion.addProperty("model", "Model");
    //		elements.add(jnotion);
    //	    } else if (notion instanceof Concept) {
    //		jnotion.addProperty("ontology", "Ontology");
    //		concepts.add(jnotion);
    //	    }
    //	    i++;
    //	}
    //	root.add("elements", elements);
    //	root.add("concepts", concepts);
    //	return root;
    //    }
}
