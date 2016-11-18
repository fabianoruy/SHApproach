package shmapper.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shmapper.model.SHInitiative;

/** Servlet implementation class AstahParseServlet */
@WebServlet("/InitiativeStartServlet")
public class InitiativeStartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private SHInitiative initiative = null;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println(">InitiativeStartServlet: " + request.getParameter("action"));

		if (request.getParameter("action").equals("login")) {
			// Verifying the password and recovering the initiative (if exists)
			String title = (String) request.getParameter("user");
			String pword = (String) request.getParameter("pword");
			System.out.println("Login: " + title + ", " + pword);

			// Recovering and SETTING the initiative to the SESSION. //
			//-------------------------------------------------------//
			this.initiative = recoverInitiative(title, pword);
			request.getSession().setAttribute("initiative", initiative);
			//-------------------------------------------------------//

			System.out.printf("Initiative: %s, %s, %s, %s", initiative.getDomain(), initiative.getPurpose(), initiative.getScope(), initiative.getPeople());
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
	}

	private SHInitiative recoverInitiative(String title, String pword) {
		//TODO: test pword and recover initiative
		//TODO: move to application
		return new SHInitiative(title, "Purpose of the Initiative", "Scope of the Initiative", "People involved in the Initiative", null);
	}
}