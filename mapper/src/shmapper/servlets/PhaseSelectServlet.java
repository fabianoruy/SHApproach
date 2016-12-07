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
import shmapper.model.Coverage;
import shmapper.model.DiagonalMapping;
import shmapper.model.Element;
import shmapper.model.HorizontalMapping;
import shmapper.model.Issue;
import shmapper.model.Mapping;
import shmapper.model.Match;
import shmapper.model.Notion.UFOType;
import shmapper.model.SHInitiative;
import shmapper.model.SimpleMatch;
import shmapper.model.StandardModel;
import shmapper.model.VerticalMapping;

/* Servlet implementation class PhaseSelectServlet */
@WebServlet("/PhaseSelectServlet")
public class PhaseSelectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	ManagerApp main;

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

			} else if (request.getParameter("action").equals("openResults")) {
				// Opening the results page
				main.log.println("# Harmonization Results");
				this.prepareResults(request, response);

				request.getRequestDispatcher("harmonizationresults.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("endSession")) {
				// Finishing the session.
				main.log.println("\n### APPLICATION FINISHED ### - " + new Date());
				response.getWriter().println("Application Finished!\n");
				response.getWriter().println(initiative.getContentMappings().size() + " Content Mappings were created:");
				for (Mapping map : initiative.getContentMappings()) {
					response.getWriter().println("(" + map.getClass().getSimpleName() + ") " + map.getBase() + " --> " + map.getTarget() + ": "
							+ map.getStatus() + " (" + map.getCoverage() + "%)");
				}
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

	/** Prepares the data to be shown in the results page. */
	private void prepareResults(HttpServletRequest request, HttpServletResponse response) {
		SHInitiative initiative = main.getInitiative();
		List<StandardModel> standards = initiative.getStandardCMs();
		List<VerticalMapping> vmappings = initiative.getVerticalContentMappings();
		List<DiagonalMapping> dmappings = initiative.getDiagonalContentMappings();
		List<HorizontalMapping> hmappings = initiative.getHorizontalContentMappings();

		// UFOTypes
		UFOType[] ufotypes = new UFOType[UFOType.values().length + 1];
		for (int i = 0; i < UFOType.values().length; i++) {
			ufotypes[i] = UFOType.values()[i];
		} // last is null

		///// Matrix of Vertical Matches
		Object[][][][] vmapsMatrix = new Object[vmappings.size()][ufotypes.length][][]; // HMappings x UFOTypes x Matches x Data
		Map<Element, Integer> rowspan = new HashMap<Element, Integer>();
		for (int m = 0; m < vmappings.size(); m++) {
			VerticalMapping vmap = vmappings.get(m);
			for (int t = 0; t < ufotypes.length; t++) {
				List<Match> allMatches = new ArrayList<Match>();
				// Getting the elements of each type
				for (Element elem : vmap.getBase().getElementsByUfotype(ufotypes[t])) {
					List<Match> elemMatches = vmap.getMatchesBySource(elem);
					if (!elemMatches.isEmpty()) {
						allMatches.addAll(elemMatches);
						rowspan.put(elem, elemMatches.size());
					} else {
						allMatches.add(new SimpleMatch(elem, null, Coverage.NOCOVERAGE, null));
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
				List<Element> elems = std.getElementsByUfotype(ufotypes[t]);
				if (elems.size() > ecount) {
					ecount = elems.size();
				}
			}
			Object[][][] elementsMatrix = new Object[ecount][standards.size()][3]; // BaseElems x Bases x Data
			for (int i = 0; i < standards.size(); i++) {
				// Getting the elements of each type
				List<Element> elems = standards.get(i).getElementsByUfotype(ufotypes[t]);
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
						elementsMatrix[j][i][0] = elem;
						elementsMatrix[j][i][1] = strmatches;
						elementsMatrix[j][i][2] = initiative.getCoverageSituation(elem);
					} else {
						elementsMatrix[j][i][0] = null;
						elementsMatrix[j][i][1] = null;
						elementsMatrix[j][i][2] = "EMPTY";
					}
				}
			}
			coverageMatrix[t] = elementsMatrix;
			main.log.print(ufotypes[t] + " (" + elementsMatrix.length + ") ");
		}
		main.log.println("");

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
				for (Element elem : hmap.getBase().getElementsByUfotype(ufotypes[t])) {
					List<Match> elemMatches = hmap.getMatchesBySource(elem);
					if (!elemMatches.isEmpty()) {
						allMatches.addAll(elemMatches);
						rowspan.put(elem, elemMatches.size());
					} else {
						allMatches.add(new SimpleMatch(elem, null, Coverage.NOCOVERAGE, null));
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

		///// Coverage Index
		Object[][][] coverageIndex = new Object[standards.size()][standards.size() + 2][2]; // Bases x Targets x Data: c%, id
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