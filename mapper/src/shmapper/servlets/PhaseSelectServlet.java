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

/* Servlet implementation class PhaseSelectServlet */
@WebServlet("/PhaseSelectServlet")
public class PhaseSelectServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private SHInitiative		initiative;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(">PhaseSelectServlet");
		// Accessing the initiative from the Session
		initiative = (SHInitiative) request.getSession().getAttribute("initiative");
		
		//TODO: put it in a better location (app)
		initiative.createMappings();

		// For opening the Page.
		if (request.getParameter("action").equals("openPage")) {
			request.setAttribute("initiative", initiative);
			request.getRequestDispatcher("phaseselector.jsp").forward(request, response);

			// If the openphase button is pressed.
		} else if (request.getParameter("action").equals("openPhase")) {
			// TODO: code here
		}
	}
}
