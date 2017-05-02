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
					// Accessing Menu
					request.getRequestDispatcher("phaseselector.jsp").forward(request, response);
				}
			}
		} catch (Exception e) {
			e.printStackTrace(main.log);
		}
	}

}