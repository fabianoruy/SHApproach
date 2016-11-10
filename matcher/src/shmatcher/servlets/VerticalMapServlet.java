package shmatcher.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import shmatcher.applications.MappingApp;
import shmatcher.model.Concept;
import shmatcher.model.Coverage;
import shmatcher.model.Element;
import shmatcher.model.Ontology;
import shmatcher.model.SHInitiative;
import shmatcher.model.SeonView;
import shmatcher.model.SimpleMatch;
import shmatcher.model.StandardModel;

/* Servlet implementation class VerticalMapServlet */
@WebServlet("/VerticalMapServlet")
public class VerticalMapServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static List<SimpleMatch> allMatches = new ArrayList<SimpleMatch>(); //TODO: get from the initiative
    private SHInitiative initiative;
    private MappingApp mapp;

    /* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	// Accessing the initiative from the Session
	initiative = (SHInitiative) request.getSession().getAttribute("initiative");

	// For opening the Page.
	if (request.getParameter("action").equals("openPage")) {
	    mapp = new MappingApp(initiative);

	    // Getting the first standard for the mapping. TODO: Fix to get the selected and create the corresponding mapping.
	    String stdId = "ykzr-9a6d5429f393b13d9aebe6086e091e4d"; // change to the selected ID.
	    StandardModel std = (StandardModel) initiative.getPackage(stdId);
	    System.out.println("# CMMI Standard: " + std);
	    SeonView seon = initiative.getSeonView();

	    // Creating the JSON
	    JsonElement json = createNotionsJSON(std, seon);
	    System.out.println(new Gson().toJson(json));

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

	    //TODO: do that in the application
	    Element elem = (Element) initiative.getNotionById(elemId);
	    Concept conc = (Concept) initiative.getNotionById(concId);

	    SimpleMatch match = new SimpleMatch(elem, conc, Coverage.valueOf(cover), comm);
	    allMatches.add(match);
	    System.out.println("(" + allMatches.size() + ") " + match);

	    request.setAttribute("matches", allMatches);
	    request.getRequestDispatcher("matches.jsp").forward(request, response);
	}
	// String line = "<b>" + elem + "</b> " + relc + " <b>" + conc + "</b>
	// {<i>" + comm + "</i>} ";
	// System.out.println("line" + line);

	// Set content type of the response so that jQuery knows what it can
	// expect.
	// response.setContentType("text/html");
	// response.setCharacterEncoding("UTF-8");
	// response.getWriter().write(line); // Write response body.
    }

    /* Creates the JSON for the Standard Elements and SeonView Concepts. */
    private JsonElement createNotionsJSON(StandardModel std, SeonView seon) {
	//	Map<Notion, String> stdCoordsHash = mapp.createNotionsCoordsHash(std.getDiagram());
	//	Map<Notion, String> seonCoordsHash = mapp.createNotionsCoordsHash(seon.getDiagram());
	JsonObject jroot = new JsonObject();
	JsonObject jelements = new JsonObject();
	JsonObject jconcepts = new JsonObject();
	for (Element elem : std.getElements()) {
	    JsonObject jelem = new JsonObject();
	    //jelem.addProperty("id", elem.getId());
	    jelem.addProperty("name", elem.getName());
	    //jelem.addProperty("definition", elem.getDefinition().replace("'", ""));
	    jelem.addProperty("definition", elem.getDefinition().replaceAll("(\\r\\n|\\n\\r|\\r|\\n)@Ex.", " Ex."));
	    //	    jelem.addProperty("coords", stdCoordsHash.get(elem));
	    jelements.add(elem.getId(), jelem);
	}
	for (Ontology onto : seon.getOntologies()) {
	    for (Concept conc : onto.getConcepts()) {
		JsonObject jconc = new JsonObject();
		//jconc.addProperty("id", conc.getId());
		jconc.addProperty("name", conc.getName());
		jconc.addProperty("definition", conc.getDefinition().replaceAll("(\\r\\n|\\n\\r|\\r|\\n)@Ex.", " Ex."));
		//		jconc.addProperty("coords", seonCoordsHash.get(conc));
		jconcepts.add(conc.getId(), jconc);
	    }
	}
	jroot.add("elements", jelements);
	jroot.add("concepts", jconcepts);
	return jroot;

	//	for (Notion notion : initiative.getAllNotions()) {
	//	    JsonObject jnotion = new JsonObject();
	//	    jnotion.addProperty("name", notion.getName());
	//	    //	    jnotion.addProperty("definition", notion.getDefinition());
	//	    if (notion instanceof Element) {
	//		//		jnotion.addProperty("model", "Model");
	//		//		elements.add("id"+i, jnotion);
	//
	//	    } else if (notion instanceof Concept) {
	//		jnotion.addProperty("ontology", "Ontology");
	//		concepts.add("id" + i, jnotion);
	//	    }
	//	    i++;
	//	}
    }

}
