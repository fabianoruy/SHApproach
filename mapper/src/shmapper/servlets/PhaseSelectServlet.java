package shmapper.servlets;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shmapper.applications.MappingApp;
import shmapper.model.Mapping;
import shmapper.model.SHInitiative;

/* Servlet implementation class PhaseSelectServlet */
@WebServlet("/PhaseSelectServlet")
public class PhaseSelectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private SHInitiative initiative;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// System.out.println(">PhaseSelectServlet: " + request.getParameter("action"));
		// Accessing the initiative from the Session
		initiative = (SHInitiative) request.getSession().getAttribute("initiative");

		if (request.getParameter("action").equals("startSelection")) {
			// Starting the selection page.
			// Initializing the application, that creates the mappings
			MappingApp mapp = new MappingApp(initiative);
			request.getSession().setAttribute("mappingapp", mapp);
			request.setAttribute("initiative", initiative);
			request.getRequestDispatcher("phaseselector.jsp").forward(request, response);

		} else if (request.getParameter("action").equals("openSelection")) {
			// Opening the Page.
			request.setAttribute("initiative", initiative);
			request.getRequestDispatcher("phaseselector.jsp").forward(request, response);

		} else if (request.getParameter("action").equals("endSession")) {
			// Finishing the session.
			System.out.println("\n### APPLICATION FINISHED ### - " + new Date());
			response.getWriter().println("Application Finished!\n");
			response.getWriter().println(initiative.getMappings().size() + " Mappings were created:");
			for (Mapping map : initiative.getMappings()) {
				response.getWriter().println(map + ": " + map.getStatus() + " (" + map.getCoverage() + "%)");
			}
			//TODO: put the logfile here. Needs to be HTML. 
			request.getSession().invalidate();
		}
	}
}
