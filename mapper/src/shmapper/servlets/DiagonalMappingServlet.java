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
import shmapper.model.VerticalMapping;

/* Servlet implementation class DiagonalMappingServlet */
@WebServlet("/DiagonalMappingServlet")
public class DiagonalMappingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private SHInitiative initiative;
	private MappingApp mapp;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (request.getParameter("action").equals("startMapping")) {
				// Starting the mapping.
				System.out.println("\n# Diagonal Mapping");

				// Accessing the initiative and application from the Session
				initiative = (SHInitiative) request.getSession().getAttribute("initiative");
				mapp = (MappingApp) request.getSession().getAttribute("mappingapp");
				DiagonalMapping dmapping = initiative.getDiagonalContentMappings().get(0); // Setting a diagonal mapping
				mapp.setCurrentMapping(dmapping);

				List<VerticalMapping> vmappings = initiative.getVerticalContentMappings();
				int mcount = vmappings.size();

				UFOType[] ufotypes = new UFOType[UFOType.values().length + 1];
				for (int i = 0; i < UFOType.values().length; i++) {
					ufotypes[i] = UFOType.values()[i];
				}
				// UFOType[] ufotypes = UFOType.values();

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
									strmatches += matches.size() + (matches.size() == 1 ? " match" : " matches")
											+ " in Vertical Mapping:\n";
									for (Match match : matches) {
										strmatches += match + "\n{" + match.getComment() + "}\n";
									}
								}
								matches = initiative.getAllDiagonalMatches(elem);
								if (!matches.isEmpty()) {
									strmatches += matches.size() + (matches.size() == 1 ? " match" : " matches")
											+ " in ICM Mapping:\n";
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
					System.out.println(ufotypes[t] + " elements:\n" + elements);
				}

				// Setting attributes and calling the page
				request.setAttribute("ufotypes", ufotypes);
				request.setAttribute("typesMatrix", typesMatrix);

				request.getRequestDispatcher("diagonalmapper.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("update")) {
				// Updating the page with the current mapping
				updatePage(request, response);

			} else if (request.getParameter("action").equals("create")) {
				// Creating a new ICM Element
				String name = request.getParameter("elemname");
				String typeId = request.getParameter("ismtype");
				String definition = request.getParameter("elemdef");
				String[][] selectedElems = new Gson().fromJson(request.getParameter("elems"), String[][].class);
				mapp.createICMElement(name, definition, typeId, selectedElems, false);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("compositeMatch")) {
				// Creating a new Composite Match.
				String elemId = request.getParameter("elem");
				String cover = request.getParameter("cover");
				mapp.createCompositeMatch(elemId, cover);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("remove")) {
				// Removing a Match
				String elemId = request.getParameter("elemId");
				mapp.removeMatch(elemId);

				updatePage(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* Updates the verticalmapper page via ajax. */
	private void updatePage(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Setting attributes and calling the page
		if (mapp != null) {
			request.setAttribute("message", mapp.getMessage());
			request.setAttribute("question", mapp.getQuestion());
			request.setAttribute("qtype", mapp.getQuestionType());
			request.setAttribute("mapping", mapp.getCurrentMapping());
			request.getRequestDispatcher("icmelements.jsp").forward(request, response);
		}
	}

}