package shmapper.servlets;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shmapper.applications.ManagerApp;
import shmapper.applications.MappingApp;
import shmapper.applications.StructuralMappingApp;
import shmapper.model.AnalysisDecision;
import shmapper.model.Concept;
import shmapper.model.DiagonalMapping;
import shmapper.model.Element;
import shmapper.model.HorizontalMapping;
import shmapper.model.Issue;
import shmapper.model.Mapping;
import shmapper.model.Match;
import shmapper.model.MatchType;
import shmapper.model.Notion.UFOType;
import shmapper.model.SHInitiative;
import shmapper.model.SimpleMatch;
import shmapper.model.StandardModel;
import shmapper.model.VerticalMapping;

/* Servlet implementation class PhaseSelectServlet */
@WebServlet("/PhaseSelectServlet")
public class PhaseSelectServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	ManagerApp					main;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			// Accessing the main app from the Session
			main = (ManagerApp) request.getSession().getAttribute("main");
			SHInitiative initiative = main.getInitiative();
			request.setAttribute("initiative", main.getInitiative());

			if (request.getParameter("action").equals("openSelection")) {
				// Starting the selection page.
				main.log.println("\n# Phase Selection");
				request.getRequestDispatcher("phaseselector.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("doStructuralMapping")) {
				// Processing the STRUCTURAL MAPPINGS (including matches)
				StructuralMappingApp smapper = main.getStructMapper();
				smapper.performStructuralMapping();

				// Creating CONTENT MAPPINGS
				MappingApp mapper = main.getMapper();
				mapper.createContentMappings();

				request.getRequestDispatcher("phaseselector.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("getStructuralMappings")) {
				// Preparing the table with the Structural Mappings (and Matches)
				this.prepareStructuralMappingsTable(request);

				// request.getRequestDispatcher("phaseselector.jsp").forward(request, response);
				request.getRequestDispatcher("smatches.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("openResults")) {
				// Opening the results page
				main.log.println("# Harmonization Results");
				this.prepareResults(request);

				request.getRequestDispatcher("harmonizationresults.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("endSession")) {
				// Finishing the session.
				main.log.println("\n### APPLICATION FINISHED ### - " + new Date());
				response.getWriter().println("Application Finished!\n");
				response.getWriter().println(initiative.getContentMappings().size() + " Content Mappings were created:");
				for (Mapping map : initiative.getContentMappings()) {
					response.getWriter().println(
							"(" + map.getClass().getSimpleName() + ") " + map.getBase() + " --> " + map.getTarget() + ": " + map.getStatus() + " (" + map.getCoverage() + "%)");
				}
				// Writing the nodes files
				// new GraphDataWriter(initiative, main.getMapperpath() + main.getInitpath() +
				// "data/").generateDataFiles();

				// TODO: put the logfile here. Needs to be HTML.
				request.getSession().invalidate();

			} else {
				main.log.println(">PhaseSelectServlet, invalid action: " + request.getParameter("action"));
			}

			// Saving Initiative
			initiative.saveInitiative();
			main.log.println(".");

		} catch (Exception e) {
			e.printStackTrace(main.log);
		}
	}

	/** Prepares the matrix of data for the Structural Mappings table. */
	private void prepareStructuralMappingsTable(HttpServletRequest request) {
		// Selecting the core concepts
		List<Concept> allConcepts = main.getInitiative().getSeonView().getConcepts();
		List<Concept> coreConcepts = new ArrayList<>();
		for (Concept conc : allConcepts) {
			if (conc.isBasetype())
				coreConcepts.add(conc);
		}
		// Getting the Vertical Mappings
		List<VerticalMapping> vmappings = main.getInitiative().getVerticalStructuralMappings();

		// Creating header
		String[] matchesLine = new String[vmappings.size() + 1];
		matchesLine[0] = "SEON Core Concept";
		for (int i = 0; i < vmappings.size(); i++) {
			matchesLine[i + 1] = vmappings.get(i).getBase().getName() + " Element";
		}
		request.setAttribute("smaptableHeader", matchesLine);

		List<String[]> matchesTable = new ArrayList<String[]>();
		// For each Core Concept, find its matches in all mappings
		for (Concept concept : coreConcepts) {
			boolean hasMatches = false;
			matchesLine = new String[vmappings.size() + 1];
			matchesLine[0] = concept.getName() + " (" + concept.getIndirectUfotype() + ")";
			// For each Vertical Mapping, get the corresponding matches
			for (int i = 0; i < vmappings.size(); i++) {
				List<SimpleMatch> matches = vmappings.get(i).getSimpleMatchesByTarget(concept);
				// For each match, put the source name in the cell
				matchesLine[i + 1] = "";
				for (SimpleMatch match : matches) {
					matchesLine[i + 1] += match.getSource().getName() + " (" + match.getSource().getIndirectUfotype() + ")<br/>";
					hasMatches = true;
				}
			}
			if (hasMatches) {
				matchesTable.add(matchesLine);
			}
		}

		// Selecting the ISM Elements
		List<Element> ISMElements = main.getInitiative().getIntegratedSM().getElements();
		// Getting the Diagonal Mappings
		List<DiagonalMapping> dmappings = main.getInitiative().getDiagonalStructuralMappings();

		// For each ISM Element, find its matches in all mappings
		for (Element elem : ISMElements) {
			boolean hasMatches = false;
			matchesLine = new String[dmappings.size() + 1];
			matchesLine[0] = "<i>" + elem.getName() + "* (" + elem.getIndirectUfotype() + ")</i>";
			// For each Diagonal Mapping, get the corresponding matches
			for (int i = 0; i < dmappings.size(); i++) {
				List<SimpleMatch> matches = dmappings.get(i).getSimpleMatchesByTarget(elem);
				// For each match, put the source name in the cell
				matchesLine[i + 1] = "";
				for (SimpleMatch match : matches) {
					matchesLine[i + 1] += match.getSource().getName() + " (" + match.getSource().getIndirectUfotype() + ")<br/>";
					hasMatches = true;
				}
			}
			if (hasMatches) {
				matchesTable.add(matchesLine);
			}
		}

		request.setAttribute("smaptable", matchesTable);
		System.out.println("Table " + matchesTable.size() + " lines");
	}

	private List<Element> getValidElementsByUfotype(Mapping map, UFOType type) {
		List<Element> elems = map.getBase().getElementsByUfotype(type);
		List<Element> result = new ArrayList<>();
		for (Element elem : elems) {
			if (!map.getInitiative().isDiscarded(elem))
				result.add(elem);
		}
		return result;
	}

	/** Prepares the data to be shown in the results page. */
	private void prepareResults(HttpServletRequest request) {
		SHInitiative initiative = main.getInitiative();
		List<StandardModel> standards = initiative.getStandardCMs();
		List<VerticalMapping> vmappings = initiative.getVerticalContentMappings();
		List<DiagonalMapping> dmappings = initiative.getDiagonalContentMappings();
		List<HorizontalMapping> hmappings = initiative.getHorizontalContentMappings();

		for (StandardModel std : standards) {
			std.sortElementsByPresentation();
		}

		// UFOTypes
		UFOType[] ufotypes = new UFOType[UFOType.values().length + 1];
		for (int i = 0; i < UFOType.values().length; i++) {
			ufotypes[i] = UFOType.values()[i];
		} // last is null

		///// Matrix of Vertical Matches
		Object[][][][] vmapsMatrix = new Object[vmappings.size()][ufotypes.length][][]; // HMappings x UFOTypes x
																						// Matches x Data
		Map<Element, Integer> rowspan = new HashMap<Element, Integer>();
		for (int m = 0; m < vmappings.size(); m++) {
			VerticalMapping vmap = vmappings.get(m);
			for (int t = 0; t < ufotypes.length; t++) {
				List<Match> allMatches = new ArrayList<Match>();
				// Getting the elements of each type
				for (Element elem : getValidElementsByUfotype(vmap, ufotypes[t])) {
					List<Match> elemMatches = vmap.getMatchesBySource(elem);
					if (!elemMatches.isEmpty()) {
						allMatches.addAll(elemMatches);
						rowspan.put(elem, elemMatches.size());
					} else {
						allMatches.add(new SimpleMatch(elem, null, MatchType.NORELATION, null));
						rowspan.put(elem, 1);
					}
				}
				Object[][] matches = new Object[allMatches.size()][4];
				int lines = 0, count = 0;
				for (int k = 0; k < allMatches.size(); k++) {
					Match match = allMatches.get(k);
					if (count == 0) {
						lines = rowspan.get(match.getSource());
						count = lines;
					}
					matches[k][0] = match;
					matches[k][1] = vmap.getCoverageSituation(match.getSource());
					matches[k][2] = lines;
					count--;
					lines = 0;
				}
				vmapsMatrix[m][t] = matches;
			}
		}

		///// Matrix of ICM Elements (and related matches)
		List<Element> elements = initiative.getIntegratedCM().getElements();
		Object[][] dmapsMatrix = new Object[elements.size()][2]; // ICM Element x Data (element, matches)
		for (int i = 0; i < elements.size(); i++) {
			List<Match> matches = new ArrayList<Match>();
			for (DiagonalMapping dmap : dmappings) {
				matches.addAll(dmap.getSimpleMatchesByTarget(elements.get(i)));
			}
			dmapsMatrix[i][0] = elements.get(i);
			dmapsMatrix[i][1] = matches;
		}

		///// Matrix of Elements Coverage
		Object[][][][] coverageMatrix = new Object[ufotypes.length][][][]; // UFOType x BaseElems x Bases x Data
		for (int t = 0; t < ufotypes.length; t++) {
			// Determining the max row number for the type (Elements)
			int ecount = 0;
			for (StandardModel std : initiative.getStandardCMs()) {
				List<Element> elems = getValidElementsByUfotype(initiative.getVerticalContentMapping(std), ufotypes[t]);
				if (elems.size() > ecount) {
					ecount = elems.size();
				}
			}
			Object[][][] elementsMatrix = new Object[ecount][standards.size()][4]; // BaseElems x Bases x Data
			for (int i = 0; i < standards.size(); i++) {
				// Getting the elements of each type
				List<Element> elems = getValidElementsByUfotype(initiative.getVerticalContentMapping(standards.get(i)), ufotypes[t]);
				for (int j = 0; j < ecount; j++) {
					if (elems.size() > j) {
						Element elem = elems.get(j);
						List<Match> matches = initiative.getAllVerticalMatches(elem);
						String strmatches = "";
						if (!matches.isEmpty()) {
							strmatches += matches.size() + (matches.size() == 1 ? " match" : " matches") + " in Vertical Mapping:\n";
							for (Match match : matches) {
								strmatches += "\u2022 " + match + "\n";
								if (!(match.getComment() == null || match.getComment().isEmpty()))
									strmatches += "   {" + match.getComment() + "}\n";
							}
						}
						matches = initiative.getAllDiagonalMatches(elem);
						strmatches += (strmatches.isEmpty() ? "" : "\n");
						if (!matches.isEmpty()) {
							strmatches += matches.size() + (matches.size() == 1 ? " match" : " matches") + " in ICM Mapping:\n";
							for (Match match : matches) {
								strmatches += "\u2022 " + match + "\n";
							}
						}
						String strjustif = null;
						AnalysisDecision decision = initiative.getDecision(elem);
						if (decision != null) {
							strjustif = "This element is justified\n" + decision.getReason() + ": " + decision.getJustification();
						}

						elementsMatrix[j][i][0] = elem;
						elementsMatrix[j][i][1] = strmatches;
						elementsMatrix[j][i][2] = strjustif;
						elementsMatrix[j][i][3] = initiative.getCoverageSituation(elem);
					} else {
						elementsMatrix[j][i][0] = null;
						elementsMatrix[j][i][1] = null;
						elementsMatrix[j][i][2] = null;
						elementsMatrix[j][i][3] = "EMPTY";
					}
				}
			}
			coverageMatrix[t] = elementsMatrix;
			main.log.print(ufotypes[t] + " (" + elementsMatrix.length + ") ");
		}
		main.log.println("");

		///// Matrix of Analysis Decisions
		List<AnalysisDecision>[] decisionsMatrix = new List[standards.size()];
		List<AnalysisDecision> decisions = initiative.getDecisions();
		for (int i = 0; i < standards.size(); i++) {
			decisionsMatrix[i] = new ArrayList<>();
			for (AnalysisDecision decision : decisions) {
				if (decision.getElement().getModel().equals(standards.get(i))) {
					decisionsMatrix[i].add(decision);
				}
			}
		}

		///// Matrix of Horizontal Matches
		Object[][][][] hmapsMatrix = new Object[2 * hmappings.size()][ufotypes.length][][]; // HMappings x UFOTypes x
																							// Matches x Data
		List<HorizontalMapping> allhmaps = new ArrayList<>();
		for (HorizontalMapping hmap : hmappings) {
			allhmaps.add(hmap);
			allhmaps.add(hmap.getMirror());
		}
		rowspan = new HashMap<Element, Integer>();
		for (int m = 0; m < allhmaps.size(); m++) {
			HorizontalMapping hmap = allhmaps.get(m);
			for (int t = 0; t < ufotypes.length; t++) {
				List<Match> allMatches = new ArrayList<Match>();
				// Getting the elements of each type
				for (Element elem : getValidElementsByUfotype(hmap, ufotypes[t])) {
					List<Match> elemMatches = hmap.getMatchesBySource(elem);
					if (!elemMatches.isEmpty()) {
						allMatches.addAll(elemMatches);
						rowspan.put(elem, elemMatches.size());
					} else {
						allMatches.add(new SimpleMatch(elem, null, MatchType.NORELATION, null));
						rowspan.put(elem, 1);
					}
				}
				Object[][] matches = new Object[allMatches.size()][4];
				int lines = 0, count = 0;
				for (int k = 0; k < allMatches.size(); k++) {
					Match match = allMatches.get(k);
					if (count == 0) {
						lines = rowspan.get(match.getSource());
						count = lines;
					}
					matches[k][0] = match;
					matches[k][1] = hmap.getCoverageSituation(match.getSource());
					matches[k][2] = lines;
					count--;
					lines = 0;
				}
				hmapsMatrix[m][t] = matches;
			}
		}

		System.out.println("HMappings.size(): " + hmappings.size());

		///// MatchType Index
		Object[][][] coverageIndex = new Object[standards.size()][standards.size() + 2][2]; // Bases x Targets x Data:
																							// c%, id
		for (int i = 0; i < standards.size(); i++) {
			StandardModel base = standards.get(i);
			VerticalMapping vmap = initiative.getVerticalContentMapping(base);
			DiagonalMapping dmap = initiative.getDiagonalContentMapping(base);
			coverageIndex[i][0][0] = base;
			coverageIndex[i][1][0] = vmap.getCoverage() + dmap.getCoverage();
			coverageIndex[i][1][1] = "vcoverage";
			for (int j = i; j < standards.size(); j++) {
				StandardModel target = null;
				if (i < j) {
					target = standards.get(j);
					HorizontalMapping hmap = initiative.getHorizontalContentMapping(base, target);
					coverageIndex[i][j + 2][0] = hmap.getCoverage(); // coverage
					coverageIndex[i][j + 2][1] = hmap.getId(); // id
					coverageIndex[j][i + 2][0] = hmap.getMirror().getCoverage(); // target coverage
					coverageIndex[j][i + 2][1] = hmap.getMirror().getId(); // target id
					// main.log.printf("(%d,%d): %s X %s\n", i, j, base, target);
				}
			}
		}

		identifyIssues(initiative);

		// Calling the results page
		request.setAttribute("ufotypes", ufotypes);
		request.setAttribute("vmapsMatrix", vmapsMatrix);
		request.setAttribute("dmapsMatrix", dmapsMatrix);
		request.setAttribute("decisionsMatrix", decisionsMatrix);
		request.setAttribute("hmapsMatrix", hmapsMatrix);
		request.setAttribute("allhmaps", allhmaps);
		request.setAttribute("coverageIndex", coverageIndex);
		request.setAttribute("coverageMatrix", coverageMatrix);
	}

	/** Identifies and prints the main initiative issues. */
	private void identifyIssues(SHInitiative initiative) {
		List<Mapping> allMappings = new ArrayList<Mapping>();
		allMappings.addAll(initiative.getVerticalContentMappings());
		allMappings.addAll(initiative.getDiagonalContentMappings());
		for (HorizontalMapping hmap : initiative.getHorizontalContentMappings()) {
			allMappings.add(hmap);
			allMappings.add(hmap.getMirror());
		}
		for (Mapping map : allMappings) {
			main.log.println("\n# " + map + " identified issues:");
			for (Issue issue : map.identifyIssues()) {
				main.log.println(issue);
			}
		}
		main.log.println("-------------------------------------------------------");
		for (VerticalMapping vmap : initiative.getVerticalContentMappings()) {
			main.log.println("\n# " + vmap + " identified relational issues:");
			for (Issue issue : vmap.identifyRelationalIssues()) {
				main.log.println(issue);
			}
		}
	}

}