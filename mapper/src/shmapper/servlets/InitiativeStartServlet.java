package shmapper.servlets;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shmapper.applications.ManagerApp;
import shmapper.model.User;

/** Servlet implementation class AstahParseServlet */
@WebServlet("/InitiativeStartServlet")
public class InitiativeStartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		ManagerApp main = null;
		try {
			if (request.getParameter("action").equals("login")) {
				String mapperpath = request.getSession().getServletContext().getRealPath("/");
				System.out.println("RealPath: " + mapperpath);
				ManagerApp.manageUsers(mapperpath);

				// Verifying the password and recovering the initiative (if exists)
				String login = (String) request.getParameter("user");
				String pword = (String) request.getParameter("pword");
				User user = User.validate(login, pword);

				if (user != null) {
					// Initializing the Main Application, which opens the initiative.
					main = new ManagerApp(user, mapperpath);

					///// SETTING THE MAIN APPLICATION TO THE SESSION. /////
					request.getSession().setAttribute("main", main);
					
					request.setAttribute("initiative", main.getInitiative());
					request.getRequestDispatcher("initiativestarter.jsp").forward(request, response);
				} else {
					request.setAttribute("message", "Invalid Password!");
					request.getRequestDispatcher("index.jsp").forward(request, response);
				}

				// this.startApp = new InitiativeStartApp(mapperdir);

				// RECOVERING THE INITIATIVE
//				this.initiative = startApp.openInitiative(title, pword);
//				if (initiative != null) {
					// SETTING THE INITIATIVE TO THE SESSION.
//					request.getSession().setAttribute("initiative", initiative);

					// Creating and setting the logfile and initiative directory to the session.
//					initdir = "initiative/" + title.toLowerCase().replaceAll("[^a-zA-Z0-9.-]", "") + File.separator;
//					System.out.println("initdir: " + initdir);
//					String logfile = startApp.createLogOutput();
//					request.getSession().setAttribute("initdir", initdir);
//					request.getSession().setAttribute("logfile", logfile);

//					System.out.println("\n### STARTING APPLICATION ###");
//					System.out.println("\n# Initiative Identification: " + initiative);

					// Creating and Setting the MappingApp to the Session
//					MappingApp mapp = new MappingApp(initiative);
//					request.getSession().setAttribute("mappingapp", mapp);


			} else {
				main = (ManagerApp) request.getSession().getAttribute("main");
				request.setAttribute("initiative", main.getInitiative());

				if (request.getParameter("action").equals("editInfo")) {
					// Opening page for edition
					request.getRequestDispatcher("initiativestarter.jsp").forward(request, response);

				} else if (request.getParameter("action").equals("accessMenu")) {
					// Updating initiative
					String purpose = (String) request.getParameter("purpose");
					String scope = (String) request.getParameter("scope");
					String people = (String) request.getParameter("people");
					main.getInitiative().setPurpose(purpose);
					main.getInitiative().setScope(scope);
					main.getInitiative().setPeople(people);
					main.getInitiative().saveInitiative();
					// Accessing Menu
					request.getRequestDispatcher("phaseselector.jsp").forward(request, response);
				}
			}
		} catch (Exception e) {
			e.printStackTrace(main.log);
		}
	}

}