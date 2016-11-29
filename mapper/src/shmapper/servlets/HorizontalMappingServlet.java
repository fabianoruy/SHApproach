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
import shmapper.model.Diagram;
import shmapper.model.HorizontalMapping;
import shmapper.model.Notion;
import shmapper.model.NotionPosition;
import shmapper.model.SHInitiative;

/* Servlet implementation class HorizontalMappingServlet */
@WebServlet("/HorizontalMappingServlet")
public class HorizontalMappingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private SHInitiative initiative;
	private MappingApp mapp;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			if (request.getParameter("action").equals("startMapping")) {
				System.out.println("\n# Horizontal Mapping");
				// Starting the mapping.
				// Accessing the initiative and application from the Session
				initiative = (SHInitiative) request.getSession().getAttribute("initiative");
				mapp = (MappingApp) request.getSession().getAttribute("mappingapp");

				// Getting the Mapping.
				String mapId = request.getParameter("mapId");
				HorizontalMapping mapping = (HorizontalMapping) initiative.getMappingById(mapId);
				mapp.setCurrentMapping(mapping);

				// Creating the JSON for notions data
				JsonElement baseJson = createJSON(mapping.getBase().getDiagram());
				JsonElement targJson = createJSON(mapping.getTarget().getDiagram());
				
				// Setting attributes and calling the page
				request.setAttribute("baseJson", baseJson);
				request.setAttribute("targJson", targJson);
				request.setAttribute("mapping", mapping);
				request.setAttribute("baseCoords", mapp.createNotionsCoordsHash(mapping.getBase().getDiagram()));
				request.setAttribute("targCoords", mapp.createNotionsCoordsHash(mapping.getTarget().getDiagram()));

				request.getRequestDispatcher("horizontalmapper.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("update")) {
				// Updating the page with the current mapping
				updatePage(request, response);

			} else if (request.getParameter("action").equals("match")) {
				// Creating a new Simple Match
				String sourceId = request.getParameter("source");
				String targetId = request.getParameter("target");
				String cover = request.getParameter("cover");
				String comm = request.getParameter("comm");
				boolean force = Boolean.valueOf(request.getParameter("force"));
				mapp.createHSimpleMatch(sourceId, targetId, cover, comm, force);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("compositeMatch")) {
				// Creating a new Composite Match.
				String sourceId = request.getParameter("source");
				String targetId = request.getParameter("target");
				String cover = request.getParameter("cover");
				mapp.createHCompositeMatch(sourceId, targetId, cover);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("removeMatch")) {
				// Removing a Match
				String matchId = request.getParameter("matchId");
				mapp.removeHMatch(matchId);

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
			request.getRequestDispatcher("hmatches.jsp").forward(request, response);
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
		String definition = notion.getDefinition().replaceAll("@Ex.", "Ex.").replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", " ")
				.replace("'", "").replace("\"", "");
		jobj.addProperty("definition", definition);
		jobj.addProperty("basetype", notion.getBasetypes().toString().replaceAll("\\[|\\]", ""));
		return jobj;
	}

}