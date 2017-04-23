package shmapper.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import shmapper.applications.ManagerApp;
import shmapper.applications.MappingApp;
import shmapper.model.DiagonalMapping;
import shmapper.model.Element;
import shmapper.model.Element.CoverageSituation;
import shmapper.model.Mapping;
import shmapper.model.Match;
import shmapper.model.Notion.UFOType;
import shmapper.model.SHInitiative;
import shmapper.model.StandardModel;
import shmapper.model.VerticalMapping;
import shmapper.model.AnalysisDecision.Reason;

/* Servlet implementation class DiagonalMappingServlet */
@WebServlet("/DiagonalMappingServlet")
public class DiagonalMappingServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	ManagerApp					main;
	private MappingApp			mapper;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			// Accessing the main app from the Session
			main = (ManagerApp) request.getSession().getAttribute("main");
			request.setAttribute("initiative", main.getInitiative());
			mapper = main.getMapper();

			if (request.getParameter("action").equals("startMapping")) {
				// Starting the mapping.
				main.log.println("\n# ICM Mapping");
				mapper.setCurrentMapping(null); // Setting a diagonal mapping

				request.getRequestDispatcher("diagonalmapper.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("update")) {
				// Updating the page with the current mapping
				updatePage(request, response);

			} else if (request.getParameter("action").equals("create")) {
				// Creating a new ICM Element
				String name = request.getParameter("name");
				String typeId = request.getParameter("ismt");
				String definition = request.getParameter("def");
				String[][] selectedElems = new Gson().fromJson(request.getParameter("elems"), String[][].class);
				boolean force = Boolean.valueOf(request.getParameter("force"));
				mapper.createICMElement(name, definition, typeId, selectedElems, force);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("remove")) {
				// Removing a Match
				String elemId = request.getParameter("elemId");
				// System.out.println("Element to be removed with all matches: " + initiative.getNotionById(elemId));
				mapper.removeICMElement(elemId);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("changeDefinition")) {
				// Changing the Element definition.
				String elemId = request.getParameter("elemId");
				String definition = request.getParameter("definition");
				mapper.changeElementDefinition(elemId, definition);
				System.out.println("Change definition: " + definition);

				updatePage(request, response);

			////// COVERAGE ANALYSIS ///////
			} else if (request.getParameter("action").equals("startAnalysis")) {
				// Starting the Coverage Analysis.
				main.log.println("\n# Coverage Analysis");
				mapper.setCurrentMapping(null); // Setting no mapping

				request.getRequestDispatcher("coverageanalysis.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("updateAnalysis")) {
				// Updating the page with the current analysis
				updateAnalysis(request, response);
				
			} else if (request.getParameter("action").equals("createDecision")) {
				// Creating a new Decison on an uncovered Element
				String id = request.getParameter("elemId");
				Reason reason = Reason.valueOf(request.getParameter("reason"));
				String justif = request.getParameter("justif");
				mapper.createDecision(id, reason, justif);

				updateAnalysis(request, response);

			}

		} catch (Exception e) {
			e.printStackTrace(main.log);
		}
	}

	/* Updates the verticalmapper page via ajax. */
	private void updatePage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SHInitiative initiative = main.getInitiative();
		// Setting attributes and calling the page
		if (mapper != null) {
			List<VerticalMapping> vmappings = initiative.getVerticalContentMappings();
			List<DiagonalMapping> dmappings = initiative.getDiagonalContentMappings();

			// Number of Base Standards
			int bcount = vmappings.size();
			// Copying UFOTypes
			UFOType[] ufotypes = new UFOType[UFOType.values().length + 1];
			for (int i = 0; i < UFOType.values().length; i++) {
				ufotypes[i] = UFOType.values()[i];
			}

			Object[][][][] typesMatrix = new Object[ufotypes.length][][][]; // UFOType x BaseElems x Bases x Data
			for (int t = 0; t < ufotypes.length; t++) {
				// Determining the max row number for the type (Elements)
				int ecount = 0;
				for (Mapping vmap : vmappings) {
					List<Element> elems = vmap.getNonFullyCoveredElementsByUfotype(ufotypes[t]);
					if (elems.size() > ecount) {
						ecount = elems.size();
					}
				}
				Object[][][] elements = new Object[ecount][bcount][3]; // BaseElems x Bases x Data
				for (int i = 0; i < bcount; i++) {
					// Getting the non covered elements of each type
					List<Element> elems = vmappings.get(i).getNonFullyCoveredElementsByUfotype(ufotypes[t]);
					for (int j = 0; j < ecount; j++) {
						if (elems.size() > j) {
							Element elem = elems.get(j);
							List<Match> matches = initiative.getAllVerticalMatches(elem);
							String strmatches = "";
							if (!matches.isEmpty()) {
								strmatches += matches.size() + (matches.size() == 1 ? " match" : " matches") + " in Vertical Mapping:\n";
								for (Match match : matches) {
									strmatches += match + "\n{" + match.getComment() + "}\n";
								}
							}
							matches = initiative.getAllDiagonalMatches(elem);
							strmatches += (strmatches.isEmpty() ? "" : "\n");
							if (!matches.isEmpty()) {
								strmatches += matches.size() + (matches.size() == 1 ? " match" : " matches") + " in ICM Mapping:\n";
								for (Match match : matches) {
									strmatches += match + "\n";
								}
							}
							elements[j][i][0] = elem;
							elements[j][i][1] = strmatches;
							elements[j][i][2] = initiative.getCoverageSituation(elem);
						} else {
							elements[j][i][0] = null;
							elements[j][i][1] = null;
							elements[j][i][2] = "EMPTY";
						}
					}
				}
				typesMatrix[t] = elements;
				// System.out.print(ufotypes[t] + " (" + elements.length + ") ");
			}
			// System.out.println("");

			// MatchType numbers
			Object[][] coverages = new Object[dmappings.size()][3];
			for (int i = 0; i < coverages.length; i++) {
				StandardModel std = dmappings.get(i).getBase();
				coverages[i][0] = std;
				coverages[i][1] = initiative.getVerticalContentMapping(std).getCoverage();
				coverages[i][2] = dmappings.get(i).getCoverage();
			}

			// ICM Elements and related Matches
			List<Element> elements = initiative.getIntegratedCM().getElements();
			Object[][] icmElements = new Object[elements.size()][2];
			for (int i = 0; i < elements.size(); i++) {
				List<Match> matches = new ArrayList<Match>();
				for (DiagonalMapping dmap : dmappings) {
					matches.addAll(dmap.getSimpleMatchesByTarget(elements.get(i)));
				}
				icmElements[i][0] = elements.get(i);
				icmElements[i][1] = matches;
			}

			// System.out.println(icmElements);
			// for (Object[] objects : icmElements) {
			// for (Object object : objects) {
			// System.out.println(object);
			// }
			// }

			// Setting attributes and calling the page
			request.setAttribute("ufotypes", ufotypes);
			request.setAttribute("typesMatrix", typesMatrix);
			request.setAttribute("coverages", coverages);
			request.setAttribute("icmelements", icmElements);
			request.setAttribute("message", mapper.getMessage());
			request.setAttribute("question", mapper.getQuestion());
			request.setAttribute("qtype", mapper.getQuestionType());
			request.getRequestDispatcher("dmatches.jsp").forward(request, response);
		}
	}

	/* Updates the coverageanalysis page via ajax. */
	private void updateAnalysis(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SHInitiative initiative = main.getInitiative();
		// Setting attributes and calling the page
		if (mapper != null) {
			List<VerticalMapping> vmappings = initiative.getVerticalContentMappings();
			List<DiagonalMapping> dmappings = initiative.getDiagonalContentMappings();

			// Copying UFOTypes
			UFOType[] ufotypes = new UFOType[UFOType.values().length + 1];
			for (int i = 0; i < UFOType.values().length; i++) {
				ufotypes[i] = UFOType.values()[i];
			}

			// Number of Base Standards
			int bcount = vmappings.size();
			// Selecting the Uncovered Elements from each base standard
			List<Element>[] elementsByBase = new List[bcount];
			for (int i = 0; i < elementsByBase.length; i++) {
				StandardModel base = initiative.getStandardCMs().get(i);
				elementsByBase[i] = new ArrayList<>();
				for (Element elem : base.getElements()) {
					CoverageSituation sit = initiative.getCoverageSituation(elem);
					if (sit != CoverageSituation.FULLY && sit != CoverageSituation.DISCARDED) {
						elementsByBase[i].add(elem);
					}
				}
			}

			Object[][][][] typesMatrix = new Object[ufotypes.length][][][]; // UFOType x BaseElems x Bases x Data
			for (int t = 0; t < ufotypes.length; t++) {
				// Determining the max row number for the type (Elements)
				int ecount = 0;
				for (List<Element> elements : elementsByBase) {
					int nonCovered = 0;
					for (Element elem : elements) {
						if (elem.getIndirectUfotype().equals(ufotypes[t]))
							nonCovered++;
					}
					if (nonCovered > ecount)
						ecount = nonCovered;
				}

				Object[][][] elements = new Object[ecount][bcount][3]; // BaseElems x Bases x Data
				for (int i = 0; i < bcount; i++) {
					// Getting the non covered elements of each type
					List<Element> elems = new ArrayList<>();
					for (Element elem: elementsByBase[i]) {
						if(elem.getIndirectUfotype().equals(ufotypes[t]))
							elems.add(elem);
					}
					for (int j = 0; j < ecount; j++) {
						if (elems.size() > j) {
							// Element
							Element elem = elems.get(j);
							// Matches
							List<Match> matches = initiative.getAllVerticalMatches(elem);
							String strmatches = "";
							if (!matches.isEmpty()) {
								strmatches += matches.size() + (matches.size() == 1 ? " match" : " matches") + " in Vertical Mapping:\n";
								for (Match match : matches) {
									strmatches += match + "\n{" + match.getComment() + "}\n";
								}
							}
							matches = initiative.getAllDiagonalMatches(elem);
							strmatches += (strmatches.isEmpty() ? "" : "\n");
							if (!matches.isEmpty()) {
								strmatches += matches.size() + (matches.size() == 1 ? " match" : " matches") + " in ICM Mapping:\n";
								for (Match match : matches) {
									strmatches += match + "\n";
								}
							}
							elements[j][i][0] = elem;
							elements[j][i][1] = strmatches;
							elements[j][i][2] = initiative.getCoverageSituation(elem);
						} else {
							elements[j][i][0] = null;
							elements[j][i][1] = null;
							elements[j][i][2] = "EMPTY";
						}
					}
				}
				typesMatrix[t] = elements;
				// System.out.print(ufotypes[t] + " (" + elements.length + ") ");
			}
			// System.out.println("");

			// MatchType numbers
			Object[][] coverages = new Object[dmappings.size()][3];
			for (int i = 0; i < coverages.length; i++) {
				StandardModel std = dmappings.get(i).getBase();
				coverages[i][0] = std;
				coverages[i][1] = initiative.getVerticalContentMapping(std).getCoverage();
				coverages[i][2] = dmappings.get(i).getCoverage();
			}

			// ICM Elements and related Matches
			List<Element> elements = initiative.getIntegratedCM().getElements();
			Object[][] icmElements = new Object[elements.size()][2];
			for (int i = 0; i < elements.size(); i++) {
				List<Match> matches = new ArrayList<Match>();
				for (DiagonalMapping dmap : dmappings) {
					matches.addAll(dmap.getSimpleMatchesByTarget(elements.get(i)));
				}
				icmElements[i][0] = elements.get(i);
				icmElements[i][1] = matches;
			}

			// System.out.println(icmElements);
			// for (Object[] objects : icmElements) {
			// for (Object object : objects) {
			// System.out.println(object);
			// }
			// }

			// Setting attributes and calling the page
			request.setAttribute("ufotypes", ufotypes);
			request.setAttribute("typesMatrix", typesMatrix);
			request.setAttribute("coverages", coverages);
			request.setAttribute("icmelements", icmElements);
			request.setAttribute("message", mapper.getMessage());
			request.setAttribute("question", mapper.getQuestion());
			request.getRequestDispatcher("adecisions.jsp").forward(request, response);
		}
	}

}