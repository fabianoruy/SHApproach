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
import shmapper.model.Diagram;
import shmapper.model.Element;
import shmapper.model.Notion;
import shmapper.model.NotionPosition;
import shmapper.model.Ontology;
import shmapper.model.SHInitiative;
import shmapper.model.SeonView;
import shmapper.model.SimpleMatch;
import shmapper.model.StandardModel;
import shmapper.model.VerticalMapping;

/* Servlet implementation class VerticalMappingServlet */
@WebServlet("/VerticalMappingServlet")
public class VerticalMappingServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private SHInitiative		initiative;
	private MappingApp			mapp;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//System.out.println(">VerticalMappingServlet: " + request.getParameter("action"));

		if (request.getParameter("action").equals("startMapping")) {
			// Starting the mapping.
			// Accessing the initiative and application from the Session
			initiative = (SHInitiative) request.getSession().getAttribute("initiative");
			mapp = (MappingApp) request.getSession().getAttribute("mappingapp");

			// Getting the Mapping.
			String mapId = request.getParameter("mapId");
			VerticalMapping mapping = (VerticalMapping) initiative.getMappingById(mapId);
			mapp.setCurrentMapping(mapping);
			StandardModel std = mapping.getBase();
			SeonView seon = mapping.getTarget();

			// Creating the JSON
			JsonElement stdJson = createJSON(std.getDiagram());
			JsonElement ontoJson = createJSON(seon.getDiagram());

			// Setting attributes and calling the page
			request.setAttribute("stdJson", stdJson);
			request.setAttribute("ontoJson", ontoJson);
			request.setAttribute("standard", std);
			request.setAttribute("ontology", seon);
			request.setAttribute("stdCoords", mapp.createNotionsCoordsHash(std.getDiagram()));
			request.setAttribute("ontoCoords", mapp.createNotionsCoordsHash(seon.getDiagram()));
			request.getRequestDispatcher("verticalmapper.jsp").forward(request, response);

		} else if (request.getParameter("action").equals("update")) {
			// Updating the page with the current mapping
			updatePage(request, response);

		} else if (request.getParameter("action").equals("match")) {
			// Creating a new Simple Match
			String elemId = request.getParameter("elem");
			String concId = request.getParameter("conc");
			String cover = request.getParameter("cover");
			String comm = request.getParameter("comm");
			SimpleMatch match = mapp.createSimpleMatch(elemId, concId, cover, comm);

			updatePage(request, response);

		} else if (request.getParameter("action").equals("compositeMatch")) {
			// Creating a new Composite Match.
			String elemId = request.getParameter("elem");
			String cover = request.getParameter("cover");
			CompositeMatch match = mapp.createCompositeMatch(elemId, cover);

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
		request.setAttribute("message", mapp.getMessage());
		request.setAttribute("question", mapp.getQuestion());
		request.setAttribute("mapping", mapp.getCurrentMapping());
		request.getRequestDispatcher("matches.jsp").forward(request, response);
	}

	/* Creates the JSON for the notions of a Diagram. */
	private JsonElement createJSON(Diagram diagram) {
		JsonObject jobj = new JsonObject();
		for (NotionPosition npos : diagram.getPositions()) {
			jobj.add(npos.getNotion().getId(), createJSON(npos.getNotion()));
		}
		return jobj;
	}

	/* Creates the JSON of a Notion. */
	private JsonObject createJSON(Notion notion) {
		JsonObject jobj = new JsonObject();
		jobj.addProperty("name", notion.getName().replace("'", ""));
		String definition = notion.getDefinition().replaceAll("@Ex.", "Ex.").replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", " ").replace("'", "");
		jobj.addProperty("definition", definition);
		jobj.addProperty("basetype", notion.getBaseType().getName());
		return jobj;
	}

}