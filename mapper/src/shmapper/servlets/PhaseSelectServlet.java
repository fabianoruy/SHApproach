package shmapper.servlets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	private SHInitiative		initiative;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		// System.out.println(">PhaseSelectServlet: " + request.getParameter("action"));
		try {
			// Accessing the initiative from the Session
			initiative = (SHInitiative) request.getSession().getAttribute("initiative");

			if (request.getParameter("action").equals("openSelection")) {
				// Starting the selection page.
				System.out.println("\n# Phase Selection");
				request.getRequestDispatcher("phaseselector.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("doStructuralMapping")) {
				String smapfile = request.getSession().getServletContext().getRealPath("/initiative") + "/structmap.txt";

				// Processing the Structural Mapping (including matches)
				StructuralMappingApp smapp = new StructuralMappingApp(initiative);
				smapp.performStructuralMapping(smapfile);

				// Creating Content Mappings
				MappingApp mapp = (MappingApp) request.getSession().getAttribute("mappingapp");
				mapp.createContentMappings();

				// initiative.saveInitiative();

				// System.out.printf("Packs %d, Maps %d\n", initiative.getAllPackages().size(),
				// initiative.getContentMappings().size());
				request.getRequestDispatcher("phaseselector.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("openResults")) {
				// Opening the results page
				System.out.println("# Harmonization Results");

				List<VerticalMapping> vmappings = initiative.getVerticalContentMappings();
				List<DiagonalMapping> dmappings = initiative.getDiagonalContentMappings();

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
					System.out.print(ufotypes[t] + " (" + elementsMatrix.length + ") ");
				}
				System.out.println("");

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
							System.out.printf("(%d,%d): %s X %s\n", i, j, base, target);
						}
					}
				}

				// Calling the results page
				request.setAttribute("ufotypes", ufotypes);
				request.setAttribute("vmapsMatrix", vmapsMatrix);
				request.setAttribute("dmapsMatrix", dmapsMatrix);
				request.setAttribute("coverageIndex", coverageIndex);
				request.setAttribute("coverageMatrix", coverageMatrix);
				request.getRequestDispatcher("harmonizationresults.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("endSession")) {
				// Finishing the session.
				System.out.println("\n### APPLICATION FINISHED ### - " + new Date());
				response.getWriter().println("Application Finished!\n");
				response.getWriter().println(initiative.getContentMappings().size() + " Content Mappings were created:");
				for (Mapping map : initiative.getContentMappings()) {
					response.getWriter().println(map + ": " + map.getStatus() + " (" + map.getCoverage() + "%)");
				}
				// TODO: put the logfile here. Needs to be HTML.
				request.getSession().invalidate();

			} else {
				System.out.println(">PhaseSelectServlet, invalid action: " + request.getParameter("action"));
			}

			// Saving Initiative
			initiative.saveInitiative();

		} catch (

		Exception e) {
			e.printStackTrace();
		}
	}

}