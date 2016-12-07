package shmapper.applications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shmapper.model.CompositeMatch;
import shmapper.model.Concept;
import shmapper.model.Issue;
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
	private ManagerApp			main;
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

	public MappingApp(ManagerApp main, SHInitiative initiative) {
		this.main = main;
		this.initiative = initiative;
	}

	/** Performs the creation of all content mappings. */
	public void createContentMappings() {
		if (initiative.getStatus() == InitiativeStatus.STRUCTURED && initiative.getContentMappings().isEmpty()) {
			this.initiative.createContentMappings();
			main.log.println("\nContent Mappings Created (" + initiative.getContentMappings().size() + "): " + initiative.getContentMappings());
		}
	}

	public void setCurrentMapping(Mapping mapping) {
		this.mapping = mapping;
		message = "";
		question = "";
		questionType = null;
		main.log.println("* Current Mapping: " + this.mapping);
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
					main.log.println("(" + mapping.getMatches().size() + ") " + match);
					if (initiative.getStatus() == InitiativeStatus.STRUCTURED) {
						initiative.setStatus(InitiativeStatus.CONTENTED);
					}
					// for asking if a Composite Match also has to be created
					checkCompositeMatch(match);
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
		main.log.println("(" + mapping.getMatches().size() + ") " + compMatch);
		return compMatch;
	}

	/** Removes a match from the mapping. */
	public void removeMatch(String matchId) {
		Match match = mapping.getMatchById(matchId);
		if (match != null) {
			if (match instanceof SimpleMatch) {
				// If there is a Composite Match related
				// CompositeMatch cmatch = mapping.getCompositeMatchByComponent((SimpleMatch) match);
				CompositeMatch cmatch = mapping.getCompositeMatchBySource(match.getSource());
				if (cmatch != null) {
					message = PROBLEM + "This match has an associated Composite Match (" + cmatch + ")!<br/>Remove it before.";
					return;
				}
			}
			mapping.removeMatch(match); // At this moment the match is excluded
			message = CHECKED + "Match <b>" + match + "</b> has been <b>removed</b> from the mapping.";
			main.log.println("Excluded: " + match);
		}
	}

	/** Replaces the comment of a given match. */
	public void changeMatchComment(String matchId, String comment) {
		Match match = mapping.getMatchById(matchId);
		if (match == null && mapping instanceof HorizontalMapping) {
			match = ((HorizontalMapping) mapping).getMirror().getMatchById(matchId);
			// if it has a mirror, not changing the mirror for now.
		}
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
		// Validates if the element (source) has not a conflictuous match (combinations different of [W] and [I]) (T1).
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

	/** Checks if a given match (only vertical, just created) can lead to a composite match. */
	private void checkCompositeMatch(SimpleMatch match) {
		// Checks if the element has a set of only partial coverages ([W] or [I]) possibly leading to a Composite Match.
		Coverage cover = match.getCoverage();
		Element source = match.getSource();
		if (cover == Coverage.WIDER || cover == Coverage.INTERSECTION) {
			List<SimpleMatch> repMatches = mapping.getSimpleMatchesBySource(source);
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

	/** Checks the Standard Disjointness (T2) in a Vertical Mapping. */
	private boolean checkStandardDisjointness(VerticalMapping vmap) {
		// Checks if each element has matches leading to overlap in the Standard.

		return true;
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
		IntegratedModel icm = initiative.getIntegratedCM();
		Element elem = new Element(name, definition, type, icm);
		// System.out.println("trying to create: " + elem + ": " + definition);

		// Creating Matches
		List<Element> sources = new ArrayList<Element>();
		for (String[] elems : selectedElems) {
			sources.add((Element) initiative.getNotionById(elems[0]));
		}
		if (forceBT || validateBasetypesCorrespondences(sources, elem)) {
			main.log.println("\nNew ICM Element: " + elem + " " + elem.getBasetypes());
			Element target = elem;
			int mcount = 0;
			for (String[] elems : selectedElems) {
				Element source = (Element) initiative.getNotionById(elems[0]);
				Coverage cover = Coverage.valueOf(elems[1]);
				SimpleMatch match = new SimpleMatch(source, target, cover, null);

				main.log.println("Match: " + match);
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
		if (elem != null) {
			// Finding and removing the element matches from the proper mappings
			for (DiagonalMapping dmap : initiative.getDiagonalContentMappings()) {
				for (Match match : dmap.getSimpleMatchesByTarget(elem)) {
					dmap.removeMatch(match); // At this moment a match is excluded
				}
			}
			IntegratedModel icm = initiative.getIntegratedCM();
			icm.removeElement(elem);
			initiative.removeNotion(elem);

			message = CHECKED + "Element <b>" + elem + "</b> has been <b>removed</b> from the ICM, together with all its matches.";
			main.log.println("Excluded: " + elem);
		}
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

	/** Replaces the definition of a given element. */
	public void changeElementDefinition(String elemId, String definition) {
		Notion elem = initiative.getNotionById(elemId);
		elem.setDefinition(definition);
	}

	//////////////////////////// HORIZONTAL MAPPING ////////////////////////////

	/** Creates a new (simple) Match in a Horizontal Mapping. */
	public SimpleMatch createHSimpleMatch(String sourceId, String targetId, String coverName, String comm, boolean forceBT) {
		Element source = (Element) initiative.getNotionById(sourceId);
		Element target = (Element) initiative.getNotionById(targetId);
		Coverage cover = Coverage.valueOf(coverName);

		// Creating the match and its mirror
		SimpleMatch match = new SimpleMatch(source, target, cover, comm);
		SimpleMatch hctam = new SimpleMatch(target, source, cover.getReflex(), comm);
		HorizontalMapping mirror = ((HorizontalMapping) mapping).getMirror();
		if (validateMatchUniqueness(match)) {
			if (forceBT || validateBasetypesCorrespondence(match)) {
				mapping.addMatch(match); // At this moment the match is registered
				main.log.println("(" + mapping.getMatches().size() + "/" + mirror.getMatches().size() + ") " + match);

				mirror.addMatch(hctam); // At this moment the match is registered
				main.log.println("(" + mapping.getMatches().size() + "/" + mirror.getMatches().size() + ") " + hctam);

				message = CHECKED + "Match <b>" + match + "</b> created!";
				return match;
			}
		}
		return null;
	}

	/** Creates a new Composite Match (only [E] and [W] coverages) in a Horizontal Mapping. */
	public CompositeMatch createHCompositeMatch(String sourceId, String coverName) {
		Element source = (Element) initiative.getNotionById(sourceId);
		HorizontalMapping hmap = mapping.getBase().equals(source.getModel()) ? (HorizontalMapping) mapping : ((HorizontalMapping) mapping).getMirror();
		Coverage cover = Coverage.valueOf(coverName);
		// System.out.printf("Creating HCMatch: %s, %s\n", source, cover);

		List<SimpleMatch> smatches = hmap.getSimpleMatchesBySource(source);
		CompositeMatch compMatch = new CompositeMatch(source, cover, null, smatches);
		hmap.addMatch(compMatch); // At this moment the match is registered
		message = CHECKED + "Composite Match <b>" + compMatch + "</b> created!";
		main.log.println("(" + mapping.getMatches().size() + "/" + ((HorizontalMapping) mapping).getMirror().getMatches().size() + ") " + compMatch);
		return compMatch;
	}

	/** Checks if a given element can lead to a composite match (in a horizontal mapping). */
	public void checkHCompositeMatch(String hmapId, String sourceId) {
		HorizontalMapping hmap = mapping.getId().equals(hmapId) ? (HorizontalMapping) mapping : ((HorizontalMapping) mapping).getMirror();
		Element source = (Element) initiative.getNotionById(sourceId);
		// Checks if the element has a set of only partial coverages ([W] or [I]) possibly leading to a Composite Match.
		if (hmap.isCompositeAble(source)) {
			List<SimpleMatch> matches = hmap.getSimpleMatchesBySource(source);
			question += "The element <b>" + source + "</b> has now " + matches.size() + " matches with different targets in this mapping.<br/>";
			question += "<code>";
			// if all matches are [W], only the EQUIVALENT and NO options are available.
			questionType = QuestionType.CompositeEquivalent;
			for (SimpleMatch match : matches) {
				question += "* <b>" + match + "</b><br/>";
				if (match.getCoverage() == Coverage.INTERSECTION) {
					// if there is an [I], only the PART OF and NO options are available.
					questionType = QuestionType.CompositePartof;
				}
			}
			question += "</code><br/>";
			question += QUESTION + "Is the element <b>" + source + "</b> <b><i>fully covered<i></b> by these " + matches.size() + " targets together?";
		}
	}

	/** Removes a match from the horizontal mapping (including the mirror match). */
	public void removeHMatch(String matchId) {
		HorizontalMapping hmap = ((HorizontalMapping) mapping);
		HorizontalMapping mirror = hmap.getMirror();
		Match match = hmap.getMatchById(matchId);
		if (match == null) {
			// it is from the mirror: change sides
			mirror = ((HorizontalMapping) mapping);
			hmap = mirror.getMirror();
			match = hmap.getMatchById(matchId);
			if (match == null)
				return;
		}
		// System.out.println("Match to be excluded: " + match + " (" + matchId + ")");
		Element source = match.getSource();
		// Verifying if there is an associated composite match
		if (match instanceof SimpleMatch) {
			CompositeMatch cmatch = hmap.getCompositeMatchBySource(source);
			if (cmatch != null) {
				message = PROBLEM + "This match has an associated Composite Match (" + cmatch + ")!<br/>Remove it before.";
				return;
			}
			Element target = (Element) ((SimpleMatch) match).getTarget();
			CompositeMatch chctam = mirror.getCompositeMatchBySource(target);
			if (chctam != null) {
				message = PROBLEM + "This match has an associated Composite Match (" + chctam + ") in the mirror Mapping!<br/>Remove it before.";
				return;
			}
			Match mmatch = mirror.getSimpleMatch(target, source);
			mirror.removeMatch(mmatch); // At this moment the mirror match is excluded
			main.log.println("(" + mapping.getMatches().size() + "/" + ((HorizontalMapping) mapping).getMirror().getMatches().size() + ") Excluded: " + mmatch);
		}
		hmap.removeMatch(match); // At this moment the match is excluded
		message = CHECKED + "Match <b>" + match + "</b> has been <b>removed</b> from the mapping.";
		main.log.println("(" + mapping.getMatches().size() + "/" + ((HorizontalMapping) mapping).getMirror().getMatches().size() + ") Excluded: " + match);
		if (mapping.getMatches().isEmpty()) {
			((HorizontalMapping) mapping).setDeduced(false);
		}
	}

	/** Deduces the Horizontal Mappings Matches from the Vertical and Diagonal Mappings Matches. */
	public String deduceMatches(String mappingId) {
		HorizontalMapping hmap = (HorizontalMapping) initiative.getMappingById(mappingId);

		int vmcount = deduceMatchesFromVerticalMappings(hmap);
		main.log.println(vmcount + " matches deduced from Vertical Mappings");
		main.log.println(hmap.getBase() + ": " + hmap.getCoverage() + "%;  " + hmap.getTarget() + ": " + hmap.getTargetCoverage() + "%.");
		int dmcount = deduceMatchesFromDiagonalMappings(hmap);
		main.log.println(dmcount + " matches deduced from ICM Mappings");
		main.log.println(hmap.getBase() + ": " + hmap.getCoverage() + "%;  " + hmap.getTarget() + ": " + hmap.getTargetCoverage() + "%.");
		int total = vmcount + dmcount;

		String results = "<b>Deduction Results:</b><br/><br/>";
		results += "A total of <b style='color:blue'>" + (total / 2)
				+ " matches</b> have been created from the previous mappings with the <b>SEON View</b> and the <b>ICM</b>.<br/>";
		results += "It represents a coverage of <b style='color:blue'>" + hmap.getCoverage() + "% for " + hmap.getBase() + "</b> and <b style='color:blue'>"
				+ hmap.getTargetCoverage() + "% for " + hmap.getTarget() + "</b>.<br/>";
		results += "<br/><b>Please, check if these matches are correct</b> (in the main and mirror mapping) and proceed with the Horizontal Mapping:<br/>";
		results += " - check the relations;<br/> - fill the required comments;<br/> - create new matches;<br/> - check the composite matches.";
		hmap.setDeduced(true);
		return results;
	}

	/** Deduces the HMs Matches from the given Base and Target Standards, using the Vertical Mappings Matches. */
	private int deduceMatchesFromVerticalMappings(HorizontalMapping hmapping) {
		StandardModel stdBase = hmapping.getBase();
		StandardModel stdTarget = hmapping.getTarget();

		// Identifying the Base VM (with the HM base as base)
		VerticalMapping vmapBase = initiative.getVerticalContentMapping(stdBase);
		// Identifying the Target VM (with the HM target as base)
		VerticalMapping vmapTarg = initiative.getVerticalContentMapping(stdTarget);

		int mcount = 0;
		// For each element from the HM Base
		for (Element source : stdBase.getElements()) {
			for (SimpleMatch bvmatch : vmapBase.getSimpleMatchesBySource(source)) {
				// Get all EQUIVALENT matches in the Base VM
				Notion concept = bvmatch.getTarget();
				// Use the matches targets to get the source element in the Target VM
				for (SimpleMatch tvmatch : vmapTarg.getSimpleMatchesByTarget(concept)) {
					Element target = tvmatch.getSource();
					String bcomment = null, tcomment = null;
					// Calculating the coverage
					Coverage bcover = bvmatch.getCoverage();
					Coverage tcover = tvmatch.getCoverage();
					Coverage cover = sumCoverage(bcover, tcover);
					if (cover != null) {
						// Create new match: source from Base VM, target form Target VM
						if (tcover == Coverage.EQUIVALENT && (bcover == Coverage.WIDER || bcover == Coverage.INTERSECTION))
							bcomment = bvmatch.getComment();
						SimpleMatch match = new SimpleMatch(source, target, cover, bcomment);
						match.setDeduced(true);
						hmapping.addMatch(match);

						// And the corresponding mirror
						if (bcover == Coverage.EQUIVALENT && (tcover == Coverage.WIDER || tcover == Coverage.INTERSECTION))
							tcomment = tvmatch.getComment();
						SimpleMatch hctam = new SimpleMatch(target, source, cover.getReflex(), tcomment);
						hctam.setDeduced(true);
						hmapping.getMirror().addMatch(hctam);

						mcount += 2;
					}
				}
			}
		}
		return mcount;
	}

	/** Deduces the HMs Matches from the given Base and Target Standards, using the Diagonal Mappings Matches. */
	private int deduceMatchesFromDiagonalMappings(HorizontalMapping hmapping) {
		StandardModel stdBase = hmapping.getBase();
		StandardModel stdTarget = hmapping.getTarget();

		// Identifying the Base VM (with the HM base as base)
		DiagonalMapping dmapBase = initiative.getDiagonalContentMapping(stdBase);
		// Identifying the Target VM (with the HM target as base)
		DiagonalMapping dmapTarg = initiative.getDiagonalContentMapping(stdTarget);

		int mcount = 0;
		// For each element from the HM Base
		for (Element source : stdBase.getElements()) {
			for (SimpleMatch bdmatch : dmapBase.getSimpleMatchesBySource(source)) {
				// Get all EQUIVALENT matches in the Base DM
				Notion concept = bdmatch.getTarget();
				// Use the matches targets to get the source element in the Target DM
				for (SimpleMatch tdmatch : dmapTarg.getSimpleMatchesByTarget(concept)) {
					Element target = tdmatch.getSource();
					// Calculating the coverage
					Coverage cover = sumCoverage(bdmatch.getCoverage(), tdmatch.getCoverage());
					if (cover != null) {
						// Create new match: source from Base VM, target form Target VM
						SimpleMatch match = new SimpleMatch(source, target, cover, null);
						match.setDeduced(true);
						hmapping.addMatch(match);
						// And the corresponding mirror
						SimpleMatch hctam = new SimpleMatch(target, source, cover.getReflex(), null);
						hctam.setDeduced(true);
						hmapping.getMirror().addMatch(hctam);
						mcount += 2;
					}
				}
			}
		}
		return mcount;
	}

	/** Returns the 'sum' of two coverages, considering Table 3 (T3 - Deductions). */
	private Coverage sumCoverage(Coverage coverA, Coverage coverB) {
		if (coverA == Coverage.EQUIVALENT) // E+E=E; E+P=W; E+W=P; E+I=I;
			return coverB.getReflex();
		if (coverB == Coverage.EQUIVALENT) // P+E=P; W+E=W; I+E=I;
			return coverA;
		if (coverA == Coverage.PARTIAL && coverB == Coverage.WIDER) // P+W=P;
			return coverA;
		if (coverA == Coverage.WIDER && coverB == Coverage.PARTIAL) // W+P=W
			return coverA;
		return null; // Other: no conclusion
	}

}