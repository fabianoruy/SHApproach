package shmapper.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import shmapper.applications.MappingApp;
import shmapper.model.CompositeMatch;
import shmapper.model.Concept;
import shmapper.model.Element;
import shmapper.model.Mapping;
import shmapper.model.Ontology;
import shmapper.model.SHInitiative;
import shmapper.model.SeonView;
import shmapper.model.SimpleMatch;
import shmapper.model.StandardModel;
import shmapper.model.VerticalMapping;

/* Servlet implementation class VerticalMapServlet */
@WebServlet("/VerticalMapServlet")
public class VerticalMapServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private SHInitiative		initiative;
	private MappingApp			mapp;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//System.out.println(">VerticalMapServlet");
		// Accessing the initiative from the Session
		initiative = (SHInitiative) request.getSession().getAttribute("initiative");

		// For opening the Page.
		if (request.getParameter("action").equals("openPage")) {
			// Initializing the application
			mapp = new MappingApp(initiative);

			// Creating a mapping for the first standard.
			// TODO: Fix to get the selected mapping.
			VerticalMapping mapping = null;
			String stdId = "ykzr-9a6d5429f393b13d9aebe6086e091e4d"; // change to the selected ID.
			for (Mapping map : initiative.getMappings()) {
				if (map instanceof VerticalMapping && map.getBase().equals(initiative.getPackage(stdId))) {
					mapping = (VerticalMapping) map;
				}
			}
			mapp.setCurrentMapping(mapping);
			StandardModel std = mapping.getBase();
			SeonView seon = mapping.getTarget();

			// Creating the JSON
			JsonElement json = createNotionsJSON(std, seon);

			request.setAttribute("json", json);
			request.setAttribute("standard", std);
			request.setAttribute("ontology", seon);
			request.setAttribute("stdCoords", mapp.createNotionsCoordsHash(std.getDiagram()));
			request.setAttribute("ontoCoords", mapp.createNotionsCoordsHash(seon.getDiagram()));
			request.getRequestDispatcher("verticalmapper.jsp").forward(request, response);

			// If the Match button is pressed.
		} else if (request.getParameter("action").equals("match")) {
			String elemId = request.getParameter("elem");
			String concId = request.getParameter("conc");
			String cover = request.getParameter("cover");
			String comm = request.getParameter("comm");

			SimpleMatch match = mapp.createSimpleMatch(elemId, concId, cover, comm);

			request.setAttribute("message", mapp.getMessage());
			request.setAttribute("question", mapp.getQuestion());
			request.setAttribute("matches", mapp.getCurrentMapping().getMatches());

			request.getRequestDispatcher("matches.jsp").forward(request, response);

			// If it is a Composite Match.
		} else if (request.getParameter("action").equals("compositeMatch")) {
			System.out.println("Servlet: Composite Match");
			String elemId = request.getParameter("elem");
			String cover = request.getParameter("cover");

			CompositeMatch match = mapp.createCompositeMatch(elemId, cover);

			request.setAttribute("message", mapp.getMessage());
			//request.setAttribute("question", "");
			request.setAttribute("matches", mapp.getCurrentMapping().getMatches());
			
			request.getRequestDispatcher("matches.jsp").forward(request, response);
		}
	}

	/* Creates the JSON for the Standard Elements and SeonView Concepts. */
	private JsonElement createNotionsJSON(StandardModel std, SeonView seon) {
		JsonObject jroot = new JsonObject();
		JsonObject jelements = new JsonObject();
		JsonObject jconcepts = new JsonObject();
		for (Element elem : std.getElements()) {
			JsonObject jelem = new JsonObject();
			jelem.addProperty("name", elem.getName());
			jelem.addProperty("definition", elem.getDefinition().replaceAll("(\\r\\n|\\n\\r|\\r|\\n)@Ex.", " Ex."));
			jelements.add(elem.getId(), jelem);
		}
		for (Ontology onto : seon.getOntologies()) {
			for (Concept conc : onto.getConcepts()) {
				JsonObject jconc = new JsonObject();
				jconc.addProperty("name", conc.getName());
				jconc.addProperty("definition", conc.getDefinition().replaceAll("(\\r\\n|\\n\\r|\\r|\\n)@Ex.", " Ex."));
				jconcepts.add(conc.getId(), jconc);
			}
		}
		jroot.add("elements", jelements);
		jroot.add("concepts", jconcepts);
		return jroot;
	}

}
