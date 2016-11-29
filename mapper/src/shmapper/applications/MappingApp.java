package shmapper.applications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shmapper.model.CompositeMatch;
import shmapper.model.Concept;
import shmapper.model.Coverage;
import shmapper.model.DiagonalMapping;
import shmapper.model.Diagram;
import shmapper.model.Element;
import shmapper.model.HorizontalMapping;
import shmapper.model.IntegratedModel;
import shmapper.model.Mapping;
import shmapper.model.Match;
import shmapper.model.Notion;
import shmapper.model.NotionPosition;
import shmapper.model.SHInitiative;
import shmapper.model.SHInitiative.InitiativeStatus;
import shmapper.model.SimpleMatch;
import shmapper.model.StandardModel;
import shmapper.model.VerticalMapping;

/** Responsible for providing the services for the mapping tasks. */
public class MappingApp {
	private SHInitiative		initiative;
	private Mapping				mapping;															// current mapping
	private String				message;
	private String				question;
	private QuestionType		questionType;
	public static final String	CHECKED		= "<span style='color:green'><b>(\u2713)</b></span> ";
	public static final String	PROBLEM		= "<span style='color:red'><b>(!)</b></span> ";
	public static final String	QUESTION	= "<span style='color:blue'><b>(?)</b></span> ";

	public static enum QuestionType {
		Basetype, CompositeEquivalent, CompositePartof
	}

	public MappingApp(SHInitiative initiative) {
		this.initiative = initiative;
	}

	/** Performs the creation of all content mappings. */
	public void createContentMappings() {
		if (initiative.getStatus() == InitiativeStatus.STRUCTURED && initiative.getContentMappings().isEmpty()) {
			this.initiative.createContentMappings();
			System.out.println("\nContent Mappings Created (" + initiative.getContentMappings().size() + "): " + initiative.getContentMappings());
		}
	}

	public void setCurrentMapping(Mapping mapping) {
		this.mapping = mapping;
		message = "";
		question = "";
		questionType = null;
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

	public QuestionType getQuestionType() {
		QuestionType qtype = questionType;
		questionType = null;
		return qtype;
	}

	//////////////////////////// VERTICAL MAPPING ////////////////////////////

	/** Creates a new (simple) Match. */
	public SimpleMatch createSimpleMatch(String elemId, String concId, String coverName, String comm, boolean forceBT) {
		Element source = (Element) initiative.getNotionById(elemId);
		Concept target = (Concept) initiative.getNotionById(concId);
		Coverage cover = Coverage.valueOf(coverName);

		SimpleMatch match = new SimpleMatch(source, target, cover, comm);
		if (validateMatchUniqueness(match)) {
			if (validateOntologyDisjointness(match)) {
				if (forceBT || validateBasetypesCorrespondence(match)) {
					mapping.addMatch(match); // At this moment the match is registered
					message = CHECKED + "Match <b>" + match + "</b> created!";
					System.out.println("(" + mapping.getMatches().size() + ") " + match);
					if (initiative.getStatus() == InitiativeStatus.STRUCTURED) {
						initiative.setStatus(InitiativeStatus.CONTENTED);
					}
					// for asking if a Composite Match also has to be created
					checkCompositeMatch(mapping, match);
					return match;
				}
			}
		}
		return null;
	}

	/** Creates a new Composite Match (only [E] and [W] coverages). */
	public CompositeMatch createCompositeMatch(String elemId, String coverName) {
		// TODO: FIX IT: it is allowing to create a Composite Match when we already have partially covered with an ICM
		// Element
		Element source = (Element) initiative.getNotionById(elemId);
		Coverage cover = Coverage.valueOf(coverName);
		List<SimpleMatch> smatches = mapping.getSimpleMatchesBySource(source);

		CompositeMatch compMatch = new CompositeMatch(source, cover, null, smatches);
		mapping.addMatch(compMatch); // At this moment the match is registered
		message = CHECKED + "Composite Match <b>" + compMatch + "</b> created!";
		System.out.println("(" + mapping.getMatches().size() + ") " + compMatch);
		return compMatch;
	}

	/** Removes a match from the mapping. */
	public void removeMatch(String matchId) {
		Match match = mapping.getMatchById(matchId);
		if (match instanceof SimpleMatch) {
			// If there is a Composite Match related
			// CompositeMatch cmatch = mapping.getCompositeMatchByComponent((SimpleMatch) match);
			CompositeMatch cmatch = mapping.getCompositeMatchBySource(match.getSource());
			if (cmatch != null) {
				message = PROBLEM + "This match has an associated Composite Match (" + cmatch + ")! Remove it before.";
				return;
			}
		}
		mapping.removeMatch(match); // At this moment the match is excluded
		message = CHECKED + "Match <b>" + match + "</b> has been <b>removed</b> from the mapping.";
		System.out.println("Excluded: " + match);
	}

	/** Replaces the comment of a given match. */
	public void changeMatchComment(String matchId, String comment) {
		Match match = mapping.getMatchById(matchId);
		match.setComment(comment);
	}

	/** Validates the Match Uniqueness (T0). */
	private boolean validateMatchUniqueness(SimpleMatch match) {
		// Validates if the element (source) is not already matched with the same concept (target) (T0).
		SimpleMatch omatch = mapping.getSimpleMatch(match.getSource(), match.getTarget());
		if (omatch != null) {
			message += PROBLEM + "The element <b>" + match.getSource() + "</b> is already matched with the same target: " + omatch + ".";
			return false;
		}
		return true;
	}

	/** Validates the Ontology Disjointness (T1). */
	private boolean validateOntologyDisjointness(SimpleMatch match) {
		// Validates if the element (source) is not already fully covered ([E] or [P]) by other matches (T1).
		Element source = match.getSource();

		// Verifying matches in the same Vertical Mapping AND in the correspondent Diagonal Mapping
		DiagonalMapping dmapping = initiative.getDiagonalContentMapping((StandardModel) source.getModel());
		List<Match> matches = mapping.getMatchesBySource(source);
		matches.addAll(dmapping.getMatchesBySource(source));
		for (Match omatch : matches) {
			String target = ((omatch.getMapping() instanceof VerticalMapping) ? "concept" : "<b>ICM Element</b>") + ": (" + omatch + ")";
			message += "The element <b>" + source + "</b> is already matched with other " + target + ".<br/>";
			Coverage cover = match.getCoverage();
			Coverage ocover = omatch.getCoverage();
			// both coverages must be [W] or [I]
			if (!((cover == Coverage.WIDER || cover == Coverage.INTERSECTION) && (ocover == Coverage.WIDER || ocover == Coverage.INTERSECTION))) {
				message += PROBLEM + "Multiple matches for the same Element are allowed only for combinations of WIDER and INTERSECTION coverages.";
				return false;
			}
		}
		return true;
	}

	/** Validates the Correspondences between the source and target basetypes using the structural mappings. */
	private boolean validateBasetypesCorrespondence(SimpleMatch newMatch) {
		Element source = newMatch.getSource();
		Notion target = newMatch.getTarget();
		List<Notion> sourcebts = source.getAllBasetypes(); // Elements
		List<Notion> targetbts = target.getAllBasetypes(); // Concepts/Elements
		// Looking for BTs matches
		for (Notion sbt : sourcebts) {
			for (Notion tbt : targetbts) {
				// Verifying the match
				if (initiative.getSimpleMatch((Element) sbt, tbt) != null)
					return true;
			}
		}
		question += PROBLEM + "<b>" + source + "</b> and <b>" + target + "</b> have no corresponding basetypes.<br/>";
		question += ("<code>(" + source.getBasetypes() + ") X (" + target.getBasetypes() + ")</code><br/><br/>").replaceAll("\\[|\\]", "");
		question += "<b>Do you really want to match them?</b>";
		questionType = QuestionType.Basetype;
		return false;
	}

	/** Checks if a given match (just created) can lead to a composite match. */
	private void checkCompositeMatch(Mapping map, SimpleMatch match) {
		// Checks if the element has a set of only partial coverages ([W] or [I]) possibly leading to a Composite Match.
		Coverage cover = match.getCoverage();
		Element source = match.getSource();
		if (cover == Coverage.WIDER || cover == Coverage.INTERSECTION) {
			List<SimpleMatch> repMatches = map.getSimpleMatchesBySource(source);
			if (repMatches.size() > 1) {
				question = message + "<br/><br/>";
				question += "The element <b>" + source + "</b> has now " + repMatches.size() + " matches with different targets.<br/>";
				question += "<code>";
				// if all matches are [W], only the EQUIVALENT and NO options are available.
				questionType = QuestionType.CompositeEquivalent;
				for (SimpleMatch omatch : repMatches) {
					question += "* <b>" + omatch + "</b><br/>";
					if (omatch.getCoverage() == Coverage.INTERSECTION) {
						// if there is an [I], only the PART OF and NO options are available.
						questionType = QuestionType.CompositePartof;
					}
				}
				question += "</code><br/>";
				question += QUESTION + "Is the element <b>" + source + "</b> <b><i>fully covered<i></b> by these " + repMatches.size() + " targets together?";
			}
		}
	}

	/** Creates a hash containing all the diagram notions (as keys) and their respective coords in the diagram. */
	public Map<Notion, String> createNotionsCoordsHash(Diagram diagram) {
		Map<Notion, String> coordsHash = new HashMap<Notion, String>();
		// Getting each Notion (Class) in the Diagram and its position.
		for (NotionPosition position : diagram.getPositions()) {
			Notion notion = position.getNotion();
			// it is not a Structural Element
			if (notion instanceof Concept || (notion instanceof Element && !((Element) notion).getModel().isStructural())) {
				coordsHash.put(notion, position.getCoords());
			}
		}
		return coordsHash;
	}

	//////////////////////////// DIAGONAL MAPPING ////////////////////////////

	/** Creates a new ICM Element. */
	public Element createICMElement(String name, String definition, String typeId, String[][] selectedElems, boolean forceBT) {
		// Creating new Element
		Notion type = initiative.getNotionById(typeId);
		IntegratedModel icm = ((DiagonalMapping) mapping).getTarget();
		Element elem = new Element(name, definition, type, icm);
		// System.out.println("trying to create: " + elem + ": " + definition);

		// Creating Matches
		List<Element> sources = new ArrayList<Element>();
		for (String[] elems : selectedElems) {
			sources.add((Element) initiative.getNotionById(elems[0]));
		}
		if (forceBT || validateBasetypesCorrespondences(sources, elem)) {
			System.out.println("\nNew ICM Element: " + elem + " " + elem.getBasetypes());
			Element target = elem;
			int mcount = 0;
			for (String[] elems : selectedElems) {
				Element source = (Element) initiative.getNotionById(elems[0]);
				Coverage cover = Coverage.valueOf(elems[1]);
				SimpleMatch match = new SimpleMatch(source, target, cover, null);

				System.out.println("Match: " + match);
				// Putting the match in the proper mapping
				for (DiagonalMapping dmap : initiative.getDiagonalContentMappings()) {
					if (source.getModel().equals(dmap.getBase())) {
						dmap.addMatch(match);
						mcount++;
					}
				}
			}
			icm.addElement(elem);
			initiative.addNotion(elem);
			message = CHECKED + "Element <b>" + elem + "</b> has been created with " + mcount + " related matches.";
			return elem;
		}
		return null;
	}

	/** Removes an ICM Element with all existing (diagonal) Matches. */
	public void removeICMElement(String elemId) {
		Element elem = (Element) initiative.getNotionById(elemId);

		// Finding and removing the element matches from the proper mappings
		for (DiagonalMapping dmap : initiative.getDiagonalContentMappings()) {
			for (Match match : dmap.getSimpleMatchesByTarget(elem)) {
				dmap.removeMatch(match); // At this moment a match is excluded
			}
		}
		IntegratedModel icm = ((DiagonalMapping) mapping).getTarget();
		icm.removeElement(elem);
		initiative.removeNotion(elem);

		message = CHECKED + "Element <b>" + elem + "</b> has been <b>removed</b> from the ICM, together with all its matches.";
		System.out.println("Excluded: " + elem);
	}

	/** Validates the Correspondences between basetypes of the sources and new element target using the structural
	 * mappings (for Diagonal Mappings). */
	private boolean validateBasetypesCorrespondences(List<Element> sources, Element target) {
		List<Notion> targetbts = target.getAllBasetypes(); // Concepts/Elements
		int count = 0;
		next: for (Element source : sources) {
			List<Notion> sourcebts = source.getAllBasetypes(); // Elements
			// Looking for BTs matches
			for (Notion sbt : sourcebts) {
				for (Notion tbt : targetbts) {
					// Verifying the matches
					if (initiative.getSimpleMatch((Element) sbt, tbt) != null) {
						continue next;
					}
				}
			}
			count++;
			question += ("<code>" + target + " (" + target.getBasetypes() + ") X " + source + " (" + source.getBasetypes() + ")</code><br/>").replaceAll("\\[|\\]", "");
		}
		if (count > 0) {
			question = PROBLEM + "The new Element <b>" + target + "</b> has no corresponding basetypes with " + count + " of the selected Elements:<br/>" + question;
			question += "<br/><b>Do you really want to create this Element with these related matches?</b>";
			questionType = QuestionType.Basetype;
			return false;
		}
		return true;
	}

	//////////////////////////// HORIZONTAL MAPPING ////////////////////////////

	/** Creates a new (simple) Match in a Horizontal Mapping. */
	public SimpleMatch createHSimpleMatch(String sourceId, String targetId, String coverName, String comm, boolean forceBT) {
		Element source = (Element) initiative.getNotionById(sourceId);
		Element target = (Element) initiative.getNotionById(targetId);
		Coverage cover = Coverage.valueOf(coverName);

		SimpleMatch match = new SimpleMatch(source, target, cover, comm);
		SimpleMatch hctam = new SimpleMatch(target, source, cover.getReflex(), comm);
		HorizontalMapping mirror = ((HorizontalMapping) mapping).getMirror();
		if (validateMatchUniqueness(match)) {
			if (forceBT || validateBasetypesCorrespondence(match)) {
				mapping.addMatch(match); // At this moment the match is registered
				mirror.addMatch(hctam); // At this moment the match is registered
				message = CHECKED + "Match <b>" + match + "</b> created!";
//				System.out.println("HMatchs created: " + match + " / " + hctam);
				System.out.println("(" + mapping.getMatches().size() + "/" + mirror.getMatches().size() + ") " + match);
				return match;
			}
		}
		return null;
	}

	/** Creates a new Composite Match (only [E] and [W] coverages) in a Horizontal Mapping. */
	public CompositeMatch createHCompositeMatch(String sourceId, String targetId, String coverName) {
		System.out.printf("Creating HCMatch: %s, %s, %s\n", sourceId, targetId, coverName);
		HorizontalMapping hmapping = null;
		Element source = null;
		Coverage cover = null;
		if (sourceId != null) {// change this check
			source = (Element) initiative.getNotionById(sourceId);
			hmapping = (HorizontalMapping) mapping;
			cover = Coverage.valueOf(coverName);
		} else if (targetId != null) { // change this check
			source = (Element) initiative.getNotionById(targetId);
			hmapping = ((HorizontalMapping) mapping).getMirror();
			cover = Coverage.valueOf(coverName).getReflex();
		}
		List<SimpleMatch> smatches = hmapping.getSimpleMatchesBySource(source); // TODO: only partials
		CompositeMatch compMatch = new CompositeMatch(source, cover, null, smatches);
		hmapping.addMatch(compMatch); // At this moment the match is registered
		message = CHECKED + "Composite Match <b>" + compMatch + "</b> created!";
		System.out.println("(" + mapping.getMatches().size() + "/" + ((HorizontalMapping) mapping).getMirror().getMatches().size() + ") " + compMatch);
		return compMatch;
	}

	/** Checks if a given match (just created) can lead to a composite match. */
	private void checkHCompositeMatch(Mapping map, SimpleMatch match) {
		// Checks if the element has a set of only partial coverages ([W] or [I]) possibly leading to a Composite Match.
		Coverage cover = match.getCoverage();
		Element source = match.getSource();
		if (cover == Coverage.WIDER || cover == Coverage.INTERSECTION) {
			for (Match omatch : map.getMatchesBySource(source)) {
				if (omatch.getCoverage() == Coverage.EQUIVALENT || omatch.getCoverage() == Coverage.PARTIAL) {
					return;
				}
			}
			List<SimpleMatch> repMatches = map.getSimpleMatchesBySource(source);
			if (repMatches.size() > 1) {
				question = message + "<br/><br/>";
				question += "The element <b>" + source + "</b> has now " + repMatches.size() + " matches with different targets.<br/>";
				question += "<code>";
				// if all matches are [W], only the EQUIVALENT and NO options are available.
				questionType = QuestionType.CompositeEquivalent;
				for (SimpleMatch omatch : repMatches) {
					question += "* <b>" + omatch + "</b><br/>";
					if (omatch.getCoverage() == Coverage.INTERSECTION) {
						// if there is an [I], only the PART OF and NO options are available.
						questionType = QuestionType.CompositePartof;
					}
				}
				question += "</code><br/>";
				question += QUESTION + "Is the element <b>" + source + "</b> <b><i>fully covered<i></b> by these " + repMatches.size() + " targets together?";
			}
		}
	}

	/** Removes a match from the horizontal mapping (including the mirror match). */
	public void removeHMatch(String matchId) {
		HorizontalMapping mirror = ((HorizontalMapping) mapping).getMirror();
		Match match = mapping.getMatchById(matchId);
		System.out.println("Match to be excluded: " + match + " (" + matchId + ")");
		Element source = match.getSource();
		Element target = (Element) ((SimpleMatch) match).getTarget();
		if (match instanceof SimpleMatch) {
			// If there is a Composite Match related
			CompositeMatch cmatch = mapping.getCompositeMatchBySource(source);
			if (cmatch != null) {
				message = PROBLEM + "This match has an associated Composite Match (" + cmatch + ")! Remove it before.";
				return;
			}
			CompositeMatch chctam = mirror.getCompositeMatchBySource(target);
			if (chctam != null) {
				message = PROBLEM + "This match has an associated Composite Match (" + chctam + ")! Remove it before.";
				return;
			}
		}
		mapping.removeMatch(match); // At this moment the match is excluded
		mirror.removeMatch(mirror.getSimpleMatch(target, source)); // At this moment the mirror match is excluded
		message = CHECKED + "Match <b>" + match + "</b> has been <b>removed</b> from the mapping.";
		System.out.println("(" + mapping.getMatches().size() + "/" + mirror.getMatches().size() + ") Excluded:" + match);
	}

}