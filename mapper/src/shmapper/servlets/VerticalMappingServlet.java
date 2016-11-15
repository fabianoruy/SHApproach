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
		// System.out.println(">VerticalMappingServlet");

		// For starting the mapping.
		if (request.getParameter("action").equals("startMapping")) {
			// Accessing the initiative from the Session
			initiative = (SHInitiative) request.getSession().getAttribute("initiative");
			// Initializing the application
			mapp = new MappingApp(initiative);

			// Getting the Mapping.
			String mapId = request.getParameter("mapId");
			VerticalMapping mapping = (VerticalMapping) initiative.getMappingById(mapId);
			mapp.setCurrentMapping(mapping);
			StandardModel std = mapping.getBase();
			SeonView seon = mapping.getTarget();

			// Creating the JSON
			// JsonElement json = createNotionsJSON(std, seon);
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

			// If the Match button is pressed.
		} else if (request.getParameter("action").equals("match")) {
			String elemId = request.getParameter("elem");
			String concId = request.getParameter("conc");
			String cover = request.getParameter("cover");
			String comm = request.getParameter("comm");
			SimpleMatch match = mapp.createSimpleMatch(elemId, concId, cover, comm);

			// Setting attributes and calling the page
			request.setAttribute("message", mapp.getMessage());
			request.setAttribute("question", mapp.getQuestion());
			request.setAttribute("mapping", mapp.getCurrentMapping());
			request.getRequestDispatcher("matches.jsp").forward(request, response);

			// If it is a Composite Match.
		} else if (request.getParameter("action").equals("compositeMatch")) {
			String elemId = request.getParameter("elem");
			String cover = request.getParameter("cover");
			CompositeMatch match = mapp.createCompositeMatch(elemId, cover);

			// Setting attributes and calling the page
			request.setAttribute("message", mapp.getMessage());
			request.setAttribute("mapping", mapp.getCurrentMapping());
			request.getRequestDispatcher("matches.jsp").forward(request, response);
		}
	}

	/* Creates the JSON for a Diagram, including its Notions and Positions. */
	private JsonElement createJSON(Diagram diagram) {
		JsonObject jobj = new JsonObject();
		for (NotionPosition npos : diagram.getPositions()) {
			jobj.add(npos.getNotion().getId(), createJSON(npos));
		}
		return jobj;
	}

	/* Creates the JSON of a Notion, including the position. */
	private JsonObject createJSON(NotionPosition npos) {
		JsonObject jobj = new JsonObject();
		Notion notion = npos.getNotion();
		jobj.addProperty("name", notion.getName().replace("'", ""));
		String definition = notion.getDefinition().replaceAll("@Ex.", "Ex.").replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", " ").replace("'", "");
		jobj.addProperty("definition", definition);
		jobj.addProperty("basetype", "basetype");
		//TODO: is it needed?
//		jobj.addProperty("x", npos.getXpos());
//		jobj.addProperty("y", npos.getYpos());
//		jobj.addProperty("h", npos.getHeight());
//		jobj.addProperty("w", npos.getWidth());
		return jobj;
	}

	// /* Creates the JSON for the Standard Elements and SeonView Concepts. */
	// private JsonElement createNotionsJSON(StandardModel std, SeonView seon) {
	// JsonObject jroot = new JsonObject();
	// JsonObject jelements = new JsonObject();
	// JsonObject jconcepts = new JsonObject();
	// for (Element elem : std.getElements()) {
	// jelements.add(elem.getId(), createNotionJSON(elem));
	// }
	// for (Ontology onto : seon.getOntologies()) {
	// for (Concept conc : onto.getConcepts()) {
	// jconcepts.add(conc.getId(), createNotionJSON(conc));
	// }
	// }
	// jroot.add("elements", jelements);
	// jroot.add("concepts", jconcepts);
	// return jroot;
	// }

}