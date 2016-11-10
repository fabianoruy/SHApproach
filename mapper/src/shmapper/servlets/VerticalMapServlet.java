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
import shmapper.model.Concept;
import shmapper.model.Element;
import shmapper.model.Ontology;
import shmapper.model.SHInitiative;
import shmapper.model.SeonView;
import shmapper.model.StandardModel;

/* Servlet implementation class VerticalMapServlet */
@WebServlet("/VerticalMapServlet")
public class VerticalMapServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SHInitiative initiative;
    private MappingApp mapp;

    /* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	System.out.println(">VerticalMapServlet");
	// Accessing the initiative from the Session
	initiative = (SHInitiative) request.getSession().getAttribute("initiative");

	// For opening the Page.
	if (request.getParameter("action").equals("openPage")) {
	    // Initializing the application
	    mapp = new MappingApp(initiative);

	    // Getting the first standard for the mapping. TODO: Fix to get the selected and create the corresponding mapping.
	    String stdId = "ykzr-9a6d5429f393b13d9aebe6086e091e4d"; // change to the selected ID.
	    StandardModel std = (StandardModel) initiative.getPackage(stdId);
	    System.out.println("# CMMI Standard: " + std);
	    SeonView seon = initiative.getSeonView();

	    // Creating the JSON
	    JsonElement json = createNotionsJSON(std, seon);
	    //System.out.println(new Gson().toJson(json));

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
	    
	    mapp.createMatch(elemId, concId, cover, comm);
	    
	    request.setAttribute("message", mapp.getMessage());
	    request.setAttribute("matches", mapp.getCurrentMatches());
	    
	    System.out.println("Message: "+ request.getAttribute("message"));
	    System.out.println("Matches: "+ request.getAttribute("matches"));
	    
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
