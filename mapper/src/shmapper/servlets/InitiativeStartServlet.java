package shmapper.servlets;

import java.io.File;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shmapper.applications.InitiativeStartApp;
import shmapper.applications.MappingApp;
import shmapper.model.SHInitiative;

/** Servlet implementation class AstahParseServlet */
@WebServlet("/InitiativeStartServlet")
public class InitiativeStartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private InitiativeStartApp startApp = null;
	private SHInitiative initiative = null;
	private String initdir = "";

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		// System.out.println(">InitiativeStartServlet: " + request.getParameter("action"));
		try {
			if (request.getParameter("action").equals("login")) {
				// Verifying the password and recovering the initiative (if exists)
				String title = (String) request.getParameter("user");
				String pword = (String) request.getParameter("pword");
				//System.out.println("Login: " + title + ", " + pword);

				// Initializing the Application
				String mapperdir = request.getSession().getServletContext().getRealPath("/");
				System.out.println("realpath: "+ mapperdir);
				System.out.println("contextpath: "+ request.getSession().getServletContext().getContextPath());

				this.startApp = new InitiativeStartApp(mapperdir);

				// RECOVERING THE INITIATIVE
				this.initiative = startApp.openInitiative(title, pword);
				if (initiative != null) {
					// SETTING THE INITIATIVE TO THE SESSION.
					request.getSession().setAttribute("initiative", initiative);

					// Creating and setting the logfile and initiative directory to the session.
					initdir = "initiative/" + title.toLowerCase().replaceAll("[^a-zA-Z0-9.-]", "") + File.separator;
					String logfile = startApp.createLogOutput();
					request.getSession().setAttribute("initdir", initdir);
					request.getSession().setAttribute("logfile", logfile);

					System.out.println("\n### STARTING APPLICATION ###");
					System.out.println("\n# Initiative Identification: " + initiative);

					// Creating and Setting the MappingApp to the Session
					MappingApp mapp = new MappingApp(initiative);
					request.getSession().setAttribute("mappingapp", mapp);

					request.getRequestDispatcher("initiativestarter.jsp").forward(request, response);
				} else {
					request.setAttribute("message", "Invalid Password!");
					request.getRequestDispatcher("index.jsp").forward(request, response);
				}

			} else if (request.getParameter("action").equals("editInfo")) {
				// Opening page for edition
				request.getRequestDispatcher("initiativestarter.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("accessMenu")) {
				// Updating initiative
				String purpose = (String) request.getParameter("purpose");
				String scope = (String) request.getParameter("scope");
				String people = (String) request.getParameter("people");
				this.initiative.setPurpose(purpose);
				this.initiative.setScope(scope);
				this.initiative.setPeople(people);

				request.getRequestDispatcher("phaseselector.jsp").forward(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}