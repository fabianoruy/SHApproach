package shmapper.servlets;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shmapper.applications.MappingApp;
import shmapper.model.Element;
import shmapper.model.Element.CoverateSituation;
import shmapper.model.Mapping;
import shmapper.model.Notion.UFOType;
import shmapper.model.SHInitiative;
import shmapper.model.VerticalMapping;

/* Servlet implementation class DiagonalMappingServlet */
@WebServlet("/DiagonalMappingServlet")
public class DiagonalMappingServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private SHInitiative		initiative;
	private MappingApp			mapp;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (request.getParameter("action").equals("startMapping")) {
			// Starting the mapping.
			System.out.println("\n# Diagonal Mapping");

			// Accessing the initiative and application from the Session
			initiative = (SHInitiative) request.getSession().getAttribute("initiative");
			mapp = (MappingApp) request.getSession().getAttribute("mappingapp");

			List<VerticalMapping> vmappings = initiative.getVerticalContentMappings();
			int mcount = vmappings.size();

			UFOType[] ufotypes = UFOType.values();
			Object[][][][] typesMatrix = new Object[ufotypes.length][][][];
			for (int t = 0; t < ufotypes.length - 1; t++) {
				// Determining the max row number for the type
				int ecount = 0;
				for (Mapping vmap : vmappings) {
					List<Element> elems = vmap.getNonFullyCoveredElementsByUfotype(ufotypes[t]);
					if (elems.size() > ecount) {
						ecount = elems.size();
					}
				}
				Object[][][] elements = new Object[ecount][mcount][2];
				for (int i = 0; i < mcount; i++) {
					// Getting the non covered elements of each type 
					List<Element> elems = vmappings.get(i).getNonFullyCoveredElementsByUfotype(ufotypes[t]);
					for (int j = 0; j < ecount; j++) {
						if (elems.size() > j) {
							Element elem = elems.get(j);
							elements[j][i][0] = elem;
							// Identifying the elems' situation
							elements[j][i][1] = initiative.getCoverageSituation(elem);
						} else {
							elements[j][i][0] = null;
							elements[j][i][1] = CoverateSituation.NONCOVERED;
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

			String elemId = request.getParameter("elem");
			String concId = request.getParameter("conc");
			String cover = request.getParameter("cover");
			String comm = request.getParameter("comm");
			boolean force = Boolean.valueOf(request.getParameter("force"));
			mapp.createSimpleMatch(elemId, concId, cover, comm, force);

			updatePage(request, response);

		} else if (request.getParameter("action").equals("compositeMatch")) {
			// Creating a new Composite Match.
			String elemId = request.getParameter("elem");
			String cover = request.getParameter("cover");
			mapp.createCompositeMatch(elemId, cover);

			updatePage(request, response);

		} else if (request.getParameter("action").equals("removeMatch")) {
			// Removing a Match
			String matchId = request.getParameter("matchId");
			mapp.removeMatch(matchId);

			updatePage(request, response);
		}
	}

	/* Updates the verticalmapper page via ajax. */
	private void updatePage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Setting attributes and calling the page
		if (mapp != null) {
			request.setAttribute("message", mapp.getMessage());
			request.setAttribute("question", mapp.getQuestion());
			request.setAttribute("qtype", mapp.getQuestionType());
			request.setAttribute("mapping", mapp.getCurrentMapping());
			request.getRequestDispatcher("matches.jsp").forward(request, response);
		}
	}

}