package shmapper.servlets;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shmapper.applications.MappingApp;
import shmapper.applications.StructuralMappingApp;
import shmapper.model.Mapping;
import shmapper.model.SHInitiative;

/* Servlet implementation class PhaseSelectServlet */
@WebServlet("/PhaseSelectServlet")
public class PhaseSelectServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private SHInitiative		initiative;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		// System.out.println(">PhaseSelectServlet: " + request.getParameter("action"));
		// Accessing the initiative from the Session
		initiative = (SHInitiative) request.getSession().getAttribute("initiative");

		try {
			if (request.getParameter("action").equals("openSelection")) {
				// Starting the selection page.
				System.out.println("\n# Phase Selection");
				request.getRequestDispatcher("phaseselector.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("doStructuralMapping")) {
				String smapfile = request.getSession().getServletContext().getRealPath("/initiative") + "/structmap.txt";

				// Processing the Structural Mapping (including matches)
				StructuralMappingApp smapp = new StructuralMappingApp(initiative);
				smapp.performStructuralMapping(smapfile);

				// Creating Content Mappings
				MappingApp mapp = (MappingApp) request.getSession().getAttribute("mappingapp");
				mapp.performContentMapping();

				//initiative.saveInitiative();

				//System.out.printf("Packs %d, Maps %d\n", initiative.getAllPackages().size(), initiative.getContentMappings().size());
				request.getRequestDispatcher("phaseselector.jsp").forward(request, response);
				
			} else if (request.getParameter("action").equals("openResults")) {
				// Opening the results page
				request.getRequestDispatcher("harmonizationresults.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("endSession")) {
				// Finishing the session.
				System.out.println("\n### APPLICATION FINISHED ### - " + new Date());
				response.getWriter().println("Application Finished!\n");
				response.getWriter().println(initiative.getContentMappings().size() + " Content Mappings were created:");
				for (Mapping map : initiative.getContentMappings()) {
					response.getWriter().println(map + ": " + map.getStatus() + " (" + map.getCoverage() + "%)");
				}
				// TODO: put the logfile here. Needs to be HTML.
				request.getSession().invalidate();

			} else {
				System.out.println(">PhaseSelectServlet, invalid action: " + request.getParameter("action"));
			}

			// Saving Initiative
			initiative.saveInitiative();

		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}

}