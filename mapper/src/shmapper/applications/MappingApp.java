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

import shmapper.model.CompositeMatch;
import shmapper.model.Concept;
import shmapper.model.Coverage;
import shmapper.model.Diagram;
import shmapper.model.Element;
import shmapper.model.Mapping;
import shmapper.model.Match;
import shmapper.model.Notion;
import shmapper.model.SHInitiative;
import shmapper.model.SimpleMatch;

/** Responsible for providing the services for the mapping tasks. */
public class MappingApp {
	private SHInitiative		initiative;
	private Mapping				mapping;
	private String				message;
	private String				question;
	public static final String	CHECKED		= "<span style='color:green'><b>(\u2713)</b></span> ";
	public static final String	PROBLEM		= "<span style='color:red'><b>(!)</b></span> ";
	public static final String	QUESTION	= "<span style='color:blue'><b>(?)</b></span> ";

	public MappingApp(SHInitiative initiative) {
		this.initiative = initiative;
		this.initiative.createMappings();
	}

	public void setCurrentMapping(Mapping mapping) {
		this.mapping = mapping;
	}

	public Mapping getCurrentMapping() {
		return mapping;
	}

	public String getMessage() {
		return message;
	}

	public String getQuestion() {
		return question;
	}

	/* Creates a new (simple) Match. */
	public SimpleMatch createSimpleMatch(String elemId, String concId, String coverName, String comm) {
		Element source = (Element) initiative.getNotionById(elemId);
		Concept target = (Concept) initiative.getNotionById(concId);
		Coverage cover = Coverage.valueOf(coverName);

		SimpleMatch match = new SimpleMatch(source, target, cover, comm);
		if (validateOntologyDisjointness(match)) {
			mapping.addMatch(match);
			System.out.println("(" + mapping.getMatches().size() + ") " + match);
			message = CHECKED + "Match <b>" + match + "</b> created!";
			return match;
		}
		return null;
	}

	/* Creates a new Composite Match (only [E] and [W] coverages). */
	public CompositeMatch createCompositeMatch(String elemId, String coverName) {
		Element source = (Element) initiative.getNotionById(elemId);
		Coverage cover = Coverage.valueOf(coverName);
		List<SimpleMatch> smatches = mapping.getSimpleMatches(source);

		CompositeMatch compMatch = new CompositeMatch(source, cover, null, smatches);
		mapping.addMatch(compMatch);
		message = CHECKED + "Composite Match <b>" + compMatch + "</b> created!";
		return compMatch;
	}

	/* Validates the Ontology Disjointness (T1). */
	private boolean validateOntologyDisjointness(SimpleMatch match) {
		// Checks if the element is already matched with the same concept.
		// Checks if the element is already fully covered ([E] or [P]) by other matches.
		Element source = match.getSource();
		List<SimpleMatch> repeatedMatches = new ArrayList<SimpleMatch>();
		boolean allowed = true;
		message = "";
		question = "";
		for (SimpleMatch omatch : mapping.getSimpleMatches()) {
			Element osource = omatch.getSource();
			// repeated source
			if (source.equals(osource)) {
				// repeated source and target
				if (match.getTarget().equals(omatch.getTarget())) {
					message += PROBLEM + "The element <b>" + source + "</b> is already matched with the same concept (" + omatch + ")";
					return false;
				}
				message += "The element <b>" + source + "</b> is already matched with other concept (" + omatch + ").<br/>";
				repeatedMatches.add(omatch);
				Coverage cover = match.getCoverage();
				Coverage ocover = omatch.getCoverage();
				// both coverages must be [W] or [I]
				if (!((cover == Coverage.WIDER || cover == Coverage.INTERSECTION) && (ocover == Coverage.WIDER || ocover == Coverage.INTERSECTION))) {
					allowed = false;
				}
			}
		}
		if (!allowed) {
			message += PROBLEM + "Multiple matches for the same element are allowed only for WIDER and INTERSECTION coverages.";
			return false;
		} else if (repeatedMatches.size() > 0) {
			repeatedMatches.add(match);
			question += "The element <b>" + source + "</b> has now " + repeatedMatches.size() + " matches with different concepts.<br/>";
			question += "<code>";
			for (SimpleMatch matchfor : repeatedMatches) {
				question += "* <b>" + matchfor + "</b><br/>";
			}
			question += "</code><br/>";
			question += QUESTION + "Do these " + repeatedMatches.size() + " concepts together <b>fully cover</b> the element <b>" + source + "</b>?";
		}
		return true;
	}

	/* Creates a hash containg all the diagram notions (as keys) and their respective coords in the diagram. */
	public Map<Notion, String> createNotionsCoordsHash(Diagram diagram) {
		Map<Notion, String> coordsHash = new HashMap<Notion, String>();
		// Getting each Notion (Class) in the diagram and its position.
		try {
			for (IPresentation present : diagram.getAstahDiagram().getPresentations()) {
				if (present instanceof INodePresentation && present.getType().equals("Class")) {
					INodePresentation node = (INodePresentation) present;
					Notion notion = initiative.getNotionById(((IClass) node.getModel()).getId());
					// it is not a Structural Element
					if (notion instanceof Concept || (notion instanceof Element && !((Element) notion).getStandardModel().isStructural())) {
						coordsHash.put(notion, getMapCoords(node, diagram.getAstahDiagram().getBoundRect()));
					}
				}
			}
		} catch (InvalidUsingException e) {
			e.printStackTrace();
		}
		return coordsHash;
	}

	/* Returns the String Coords of a html image MAP diagram. */
	private String getMapCoords(INodePresentation node, Rectangle2D adjust) {
		int x = (int) Math.round(node.getLocation().getX() - adjust.getX());
		int y = (int) Math.round(node.getLocation().getY() - adjust.getY());
		int w = (int) Math.round(node.getWidth());
		int h = (int) Math.round(node.getHeight());
		return "" + x + "," + y + "," + (x + w) + "," + (y + h);
	}

}
