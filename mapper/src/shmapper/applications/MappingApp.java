package shmapper.applications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shmapper.model.CompositeMatch;
import shmapper.model.Concept;
import shmapper.model.Coverage;
import shmapper.model.Diagram;
import shmapper.model.Element;
import shmapper.model.Mapping;
import shmapper.model.Match;
import shmapper.model.Notion;
import shmapper.model.NotionPosition;
import shmapper.model.SHInitiative;
import shmapper.model.SimpleMatch;

/** Responsible for providing the services for the mapping tasks. */
public class MappingApp {
	private SHInitiative initiative;
	private Mapping mapping; // current mapping
	private String message;
	private String question;
	public static final String CHECKED = "<span style='color:green'><b>(\u2713)</b></span> ";
	public static final String PROBLEM = "<span style='color:red'><b>(!)</b></span> ";
	public static final String QUESTION = "<span style='color:blue'><b>(?)</b></span> ";

	public MappingApp(SHInitiative initiative) {
		this.initiative = initiative;
		this.initiative.createMappings();
		System.out.println("Mappings Created: " + initiative.getMappings());
	}

	public void setCurrentMapping(Mapping mapping) {
		this.mapping = mapping;
		System.out.println("* Current Mapping: " + this.mapping);
	}

	public Mapping getCurrentMapping() {
		return mapping;
	}

	public String getMessage() {
		String msg = message;
		message = "";
		return msg;
	}

	public String getQuestion() {
		String quest = question;
		question = "";
		return quest;
	}

	/* Creates a new (simple) Match. */
	public SimpleMatch createSimpleMatch(String elemId, String concId, String coverName, String comm) {
		Element source = (Element) initiative.getNotionById(elemId);
		Concept target = (Concept) initiative.getNotionById(concId);
		Coverage cover = Coverage.valueOf(coverName);

		SimpleMatch match = new SimpleMatch(source, target, cover, comm);
		if (validateOntologyDisjointness(match)) {
			mapping.addMatch(match); // At this moment the match is registered
			message = CHECKED + "Match <b>" + match + "</b> created!";
			System.out.println("(" + mapping.getMatches().size() + ") " + match);
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
		mapping.addMatch(compMatch); // At this moment the match is registered
		message = CHECKED + "Composite Match <b>" + compMatch + "</b> created!";
		System.out.println("(" + mapping.getMatches().size() + ") " + compMatch);
		return compMatch;
	}

	/* Removes a match from the mapping. */
	public void removeMatch(String matchId) {
		Match match = mapping.getMatchById(matchId);
		if (match instanceof SimpleMatch) {
			// If there is a Composite Match related
			CompositeMatch cmatch = mapping.getCompositeMatch((SimpleMatch) match);
			if (cmatch != null) {
				message = PROBLEM + "This match has an associated Composite Match (" + cmatch + ")! Remove it before.";
				return;
			}
		}
		mapping.removeMatch(match); // At this moment the match is excluded
		message = CHECKED + "Match <b>" + match + "</b> has been removed from the mapping.";
		System.out.println("Excluded: " + match);
	}

	/* Validates the Ontology Disjointness (T1). */
	private boolean validateOntologyDisjointness(SimpleMatch match) {
		// Checks if the element is already matched with the same concept (T0).
		// Checks if the element is already fully covered ([E] or [P]) by other matches (ontology disjointness (T1)).
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
					message += PROBLEM + "The element <b>" + source + "</b> is already matched with the same concept ("
							+ omatch + ")";
					return false;
				}
				message += "The element <b>" + source + "</b> is already matched with other concept (" + omatch
						+ ").<br/>";
				repeatedMatches.add(omatch);
				Coverage cover = match.getCoverage();
				Coverage ocover = omatch.getCoverage();
				// both coverages must be [W] or [I]
				if (!((cover == Coverage.WIDER || cover == Coverage.INTERSECTION)
						&& (ocover == Coverage.WIDER || ocover == Coverage.INTERSECTION))) {
					allowed = false;
				}
			}
		}
		if (!allowed) {
			message += PROBLEM
					+ "Multiple matches for the same element are allowed only for combinations of WIDER and INTERSECTION coverages.";
			return false;
		} else if (repeatedMatches.size() > 0) {
			repeatedMatches.add(match);
			question += "The element <b>" + source + "</b> has now " + repeatedMatches.size()
					+ " matches with different concepts.<br/>";
			question += "<code>";
			for (SimpleMatch matchfor : repeatedMatches) {
				question += "* <b>" + matchfor + "</b><br/>";
			}
			//TODO: if all matches are [W], only the EQUIVALENT, and NO options are available.
			question += "</code><br/>";
			question += QUESTION + "Is the element <b>" + source + "</b> <b>fully covered</b> by these " + repeatedMatches.size()	+ " concepts together?";
			//question += QUESTION + "Do these " + repeatedMatches.size()	+ " concepts together <b>fully cover</b> the element <b>" + source + "</b>?";
		}
		return true;
	}

	/* Creates a hash containg all the diagram notions (as keys) and their respective coords in the diagram. */
	public Map<Notion, String> createNotionsCoordsHash(Diagram diagram) {
		Map<Notion, String> coordsHash = new HashMap<Notion, String>();
		// Getting each Notion (Class) in the Diagram and its position.
		for (NotionPosition position : diagram.getPositions()) {
			Notion notion = position.getNotion();
			// it is not a Structural Element
			if (notion instanceof Concept
					|| (notion instanceof Element && !((Element) notion).getModel().isStructural())) {
				coordsHash.put(notion, position.getCoords());
			}
		}
		return coordsHash;
	}

}