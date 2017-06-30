package shmapper.servlets;

import java.io.IOException;
import java.util.Map;

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
import shmapper.model.Element;
import shmapper.model.HorizontalMapping;
import shmapper.model.Notion;
import shmapper.model.NotionPosition;
import shmapper.model.SHInitiative;
import shmapper.model.Element.CoverageSituation;

/* Servlet implementation class HorizontalMappingServlet */
@WebServlet("/HorizontalMappingServlet")
public class HorizontalMappingServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private MappingApp			mapper;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		ManagerApp main = null;
		try {
			// Accessing the main app from the Session
			main = (ManagerApp) request.getSession().getAttribute("main");
			SHInitiative initiative = main.getInitiative();
			mapper = main.getMapper();
			request.setAttribute("initiative", main.getInitiative());

			if (request.getParameter("action").equals("startMapping")) {
				// Starting the mapping.
				main.log.println("\n# Horizontal Mapping");

				// Getting the Selected Mapping.
				String mapId = request.getParameter("mapId");
				HorizontalMapping mapping = (HorizontalMapping) initiative.getMappingById(mapId);
				mapper.setCurrentMapping(mapping);

				// Creating the JSON for notions data
				JsonElement baseJson = createJSON(mapping.getBase().getDiagram());
				JsonElement targJson = createJSON(mapping.getTarget().getDiagram());

				// Setting attributes and calling the page
				request.setAttribute("baseJson", baseJson);
				request.setAttribute("targJson", targJson);
				request.setAttribute("mapping", mapping);
				request.setAttribute("baseCoords", mapper.createNotionsCoordsHash(mapping.getBase().getDiagram()));
				request.setAttribute("targCoords", mapper.createNotionsCoordsHash(mapping.getTarget().getDiagram()));

				request.getRequestDispatcher("horizontalmapper.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("update")) {
				// Updating the page with the current mapping
				updatePage(request, response);

			} else if (request.getParameter("action").equals("match")) {
				// Creating a new Simple Match
				String sourceId = request.getParameter("source");
				String targetId = request.getParameter("target");
				String type = request.getParameter("type");
				String cover = request.getParameter("cover");
				String comm = request.getParameter("comm");
				boolean force = Boolean.valueOf(request.getParameter("force"));
				if (cover == null) cover = "UNDEFINED";
				mapper.createHSimpleMatch(sourceId, targetId, type, cover, comm, force);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("checkCompositeMatch")) {
				// Checking for a new Composite Match.
				String hmapId = request.getParameter("mapping");
				String sourceId = request.getParameter("source");
				mapper.checkHCompositeMatch(hmapId, sourceId);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("compositeMatch")) {
				// Creating a new Composite Match.
				String sourceId = request.getParameter("source");
				String type = request.getParameter("type");
				mapper.createHCompositeMatch(sourceId, type);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("removeMatch")) {
				// Removing a Match
				String matchId = request.getParameter("matchId");
				mapper.removeHMatch(matchId);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("changeComment")) {
				// Changing the Match Comment.
				String matchId = request.getParameter("matchId");
				String comment = request.getParameter("comment");
				mapper.changeMatchComment(matchId, comment);
				System.out.println("Change comment: " + comment);

				updatePage(request, response);

			} else if (request.getParameter("action").equals("deduce")) {
				// Deducing the matches form previous mappings
				main.log.println("# Deducing Horizontal Matches ...");
				String mappingId = request.getParameter("mapping");
				String results = mapper.deduceMatches(mappingId);

				request.setAttribute("deductionresults", results);
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
			//Map<Element, CoverageSituation> coverages = mapper.getCurrentMapping().getElementsSituations();
			request.setAttribute("message", mapper.getMessage());
			request.setAttribute("question", mapper.getQuestion());
			request.setAttribute("qtype", mapper.getQuestionType());
			request.setAttribute("mapping", mapper.getCurrentMapping());
			//request.setAttribute("coveragelist", createJSON(coverages));
			if (!((HorizontalMapping) mapper.getCurrentMapping()).isDeduced()) {
				request.setAttribute("deductionresults", "ready");
			}
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
		String definition = notion.getDefinition().replaceAll("@Ex.", "Ex.").replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", " ").replace("'", "").replace("\"", "");
		jobj.addProperty("definition", definition);
		jobj.addProperty("basetype", notion.getBasetypes().toString().replaceAll("\\[|\\]", ""));
		return jobj;
	}
	
//	/* Creates the JSON for the elements' situations. */
//	private JsonElement createJSON(Map<Element, Element.CoverageSituation> situations) {
//		JsonObject jobj = new JsonObject();
//		for (Element elem : situations.keySet()) {
//			jobj.addProperty(elem.getId(), situations.get(elem).name());
//		}
//		return jobj;
//	}


}