package shmapper.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import shmapper.applications.ManagerApp;
import shmapper.applications.MappingApp;
import shmapper.model.Diagram;
import shmapper.model.Notion;
import shmapper.model.NotionPosition;
import shmapper.model.SeonView;
import shmapper.model.StandardModel;
import shmapper.model.VerticalMapping;

/* Servlet implementation class VerticalMappingServlet */
@WebServlet("/VerticalMappingServlet")
public class VerticalMappingServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private MappingApp			mapper;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		ManagerApp main = null;
		try {
			// Accessing the main app from the Session
			main = (ManagerApp) request.getSession().getAttribute("main");
			request.setAttribute("initiative", main.getInitiative());
			mapper = main.getMapper();

			if (request.getParameter("action").equals("startMapping")) {
				// Starting the Vertical Mapping.
				main.log.println("\n# Vertical Mapping");

				// Getting the Selected Mapping.
				String mapId = request.getParameter("mapId");
				VerticalMapping mapping = (VerticalMapping) main.getInitiative().getMappingById(mapId);
				mapper.setCurrentMapping(mapping);
				StandardModel std = mapping.getBase();
				SeonView seon = mapping.getTarget();

				// Creating the JSON for notions data
				JsonElement stdJson = createJSON(std.getDiagram());
				JsonElement ontoJson = createJSON(seon.getDiagram());

				// Setting attributes and calling the page
				request.setAttribute("stdJson", stdJson);
				request.setAttribute("ontoJson", ontoJson);
				request.setAttribute("standard", std);
				request.setAttribute("ontology", seon);
				request.setAttribute("stdCoords", mapper.createNotionsCoordsHash(std.getDiagram()));
				request.setAttribute("ontoCoords", mapper.createNotionsCoordsHash(seon.getDiagram()));

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
				boolean force = Boolean.valueOf(request.getParameter("force"));
				mapper.createSimpleMatch(elemId, concId, cover, comm, force);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("compositeMatch")) {
				// Creating a new Composite Match.
				String elemId = request.getParameter("elem");
				String cover = request.getParameter("cover");
				mapper.createCompositeMatch(elemId, cover);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("removeMatch")) {
				// Removing a Match
				String matchId = request.getParameter("matchId");
				mapper.removeMatch(matchId);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("changeComment")) {
				// Changing the Match Comment.
				String matchId = request.getParameter("matchId");
				String comment = request.getParameter("comment");
				mapper.changeMatchComment(matchId, comment);
				System.out.println("Change comment: " + comment);

				updatePage(request, response);

			}
		} catch (Exception e) {
			e.printStackTrace(main.log);
		}
	}

	/* Updates the verticalmapper page via ajax. */
	private void updatePage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Setting attributes and calling the page
		if (mapper != null) {
			request.setAttribute("message", mapper.getMessage());
			request.setAttribute("question", mapper.getQuestion());
			request.setAttribute("qtype", mapper.getQuestionType());
			request.setAttribute("mapping", mapper.getCurrentMapping());
			request.getRequestDispatcher("vmatches.jsp").forward(request, response);
		}
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
		String definition = notion.getDefinition().replaceAll("@Ex.", "Ex.").replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", " ").replaceAll("'|\"", "");
		jobj.addProperty("definition", definition);
		jobj.addProperty("basetype", notion.getBasetypes().toString().replaceAll("\\[|\\]", ""));
		return jobj;
	}

}