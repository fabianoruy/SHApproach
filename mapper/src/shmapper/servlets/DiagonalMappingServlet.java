package shmapper.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import shmapper.applications.MappingApp;
import shmapper.model.DiagonalMapping;
import shmapper.model.Element;
import shmapper.model.Element.CoverateSituation;
import shmapper.model.Mapping;
import shmapper.model.Match;
import shmapper.model.Notion.UFOType;
import shmapper.model.SHInitiative;
import shmapper.model.StandardModel;
import shmapper.model.VerticalMapping;

/* Servlet implementation class DiagonalMappingServlet */
@WebServlet("/DiagonalMappingServlet")
public class DiagonalMappingServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private SHInitiative		initiative;
	private MappingApp			mapp;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (request.getParameter("action").equals("startMapping")) {
				// Starting the mapping.
				System.out.println("\n# Diagonal Mapping");

				// Accessing the initiative and application from the Session
				initiative = (SHInitiative) request.getSession().getAttribute("initiative");
				mapp = (MappingApp) request.getSession().getAttribute("mappingapp");
				mapp.setCurrentMapping(initiative.getDiagonalContentMappings().get(0)); // Setting a diagonal mapping

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
				mapp.createICMElement(name, definition, typeId, selectedElems, false);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("remove")) {
				// Removing a Match
				String elemId = request.getParameter("elemId");
				System.out.println("Element to be removed with all matches: "+ initiative.getNotionById(elemId));
				// mapp.removeICMElement(elemId);

				updatePage(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* Updates the verticalmapper page via ajax. */
	private void updatePage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Setting attributes and calling the page
		if (mapp != null) {
			List<VerticalMapping> vmappings = initiative.getVerticalContentMappings();
			List<DiagonalMapping> dmappings = initiative.getDiagonalContentMappings();

			int mcount = vmappings.size();
			UFOType[] ufotypes = new UFOType[UFOType.values().length + 1];
			for (int i = 0; i < UFOType.values().length; i++) {
				ufotypes[i] = UFOType.values()[i];
			}

			Object[][][][] typesMatrix = new Object[ufotypes.length][][][];
			for (int t = 0; t < ufotypes.length; t++) {
				// Determining the max row number for the type
				int ecount = 0;
				for (Mapping vmap : vmappings) {
					List<Element> elems = vmap.getNonFullyCoveredElementsByUfotype(ufotypes[t]);
					if (elems.size() > ecount) {
						ecount = elems.size();
					}
				}
				Object[][][] elements = new Object[ecount][mcount][3];
				for (int i = 0; i < mcount; i++) {
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
							if (!matches.isEmpty()) {
								strmatches += matches.size() + (matches.size() == 1 ? " match" : " matches") + " in ICM Mapping:\n";
								for (Match match : matches) {
									strmatches += match + "\n{" + match.getComment() + "}\n";
								}
							}
							elements[j][i][0] = elem;
							elements[j][i][1] = strmatches;
							elements[j][i][2] = initiative.getCoverageSituation(elem);
						} else {
							elements[j][i][0] = null;
							elements[j][i][1] = null;
							elements[j][i][2] = CoverateSituation.NONCOVERED;
						}
					}
				}
				typesMatrix[t] = elements;
				System.out.println(ufotypes[t] + " lines: " + elements.length);
			}

			// Coverage numbers
			String[][] coverages = new String[dmappings.size()][3];
			for (int i = 0; i < coverages.length; i++) {
				StandardModel std = dmappings.get(i).getBase();
				coverages[i][0] = std.getName();
				coverages[i][1] = initiative.getVerticalContentMapping(std).getCoverage() + "%";
				coverages[i][2] = dmappings.get(i).getCoverage() + "%";
			}

			// Setting attributes and calling the page
			request.setAttribute("ufotypes", ufotypes);
			request.setAttribute("typesMatrix", typesMatrix);
			request.setAttribute("coverages", coverages);
			request.setAttribute("message", mapp.getMessage());
			request.setAttribute("question", mapp.getQuestion());
			request.setAttribute("qtype", mapp.getQuestionType());
			request.getRequestDispatcher("dmatches.jsp").forward(request, response);
		}
	}

}