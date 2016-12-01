package shmapper.servlets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shmapper.applications.ManagerApp;
import shmapper.applications.MappingApp;
import shmapper.applications.StructuralMappingApp;
import shmapper.model.DiagonalMapping;
import shmapper.model.Element;
import shmapper.model.HorizontalMapping;
import shmapper.model.Mapping;
import shmapper.model.Match;
import shmapper.model.Notion.UFOType;
import shmapper.model.SHInitiative;
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
					response.getWriter().println(map + ": " + map.getStatus() + " (" + map.getCoverage() + "%)");
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
		List<VerticalMapping> vmappings = initiative.getVerticalContentMappings();
		List<DiagonalMapping> dmappings = initiative.getDiagonalContentMappings();
		List<HorizontalMapping> hmappings = initiative.getHorizontalContentMappings();

		// UFOTypes
		UFOType[] ufotypes = new UFOType[UFOType.values().length + 1];
		for (int i = 0; i < UFOType.values().length; i++) {
			ufotypes[i] = UFOType.values()[i];
		} // last is null

		///// Matrix of Vertical Matches
		Object[][] vmapsMatrix = new Object[vmappings.size()][ufotypes.length]; // VMappings x UFOTypes: matches
		for (int i = 0; i < vmappings.size(); i++) {
			for (int j = 0; j < ufotypes.length; j++) {
				vmapsMatrix[i][j] = vmappings.get(i).getMatchesBySourceUfotype(ufotypes[j]);
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
		List<StandardModel> standards = initiative.getStandardCMs();
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
		Object[][] hmapsMatrix = new Object[hmappings.size()][ufotypes.length]; // HMappings x UFOTypes: matches
		for (int i = 0; i < hmappings.size(); i++) {
			for (int j = 0; j < ufotypes.length; j++) {
				hmapsMatrix[i][j] = hmappings.get(i).getMatchesBySourceUfotype(ufotypes[j]);
			}
		}
		System.out.println("HMappings.size(): "+ hmappings.size());

		///// Coverage Index
		Object[][] coverageIndex = new Object[standards.size()][standards.size() + 2]; // Bases x Targets
		for (int i = 0; i < standards.size(); i++) {
			StandardModel base = standards.get(i);
			VerticalMapping vmap = initiative.getVerticalContentMapping(base);
			DiagonalMapping dmap = initiative.getDiagonalContentMapping(base);
			coverageIndex[i][0] = base;
			coverageIndex[i][1] = vmap.getCoverage() + dmap.getCoverage();
			for (int j = i; j < standards.size(); j++) {
				StandardModel target = null;
				if (i < j) {
					target = standards.get(j);
					HorizontalMapping hmap = initiative.getHorizontalContentMapping(base, target);
					coverageIndex[i][j + 2] = hmap.getCoverage(); // base coverage
					coverageIndex[j][i + 2] = hmap.getTargetCoverage(); // target coverage
					// main.log.printf("(%d,%d): %s X %s\n", i, j, base, target);
				}
			}
		}

		// Calling the results page
		request.setAttribute("ufotypes", ufotypes);
		request.setAttribute("vmapsMatrix", vmapsMatrix);
		request.setAttribute("dmapsMatrix", dmapsMatrix);
		request.setAttribute("hmapsMatrix", hmapsMatrix);
		request.setAttribute("coverageIndex", coverageIndex);
		request.setAttribute("coverageMatrix", coverageMatrix);
	}

}