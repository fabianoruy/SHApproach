package shmapper.servlets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shmapper.model.SHInitiative;

/** Servlet implementation class AstahParseServlet */
@WebServlet("/InitiativeStartServlet")
public class InitiativeStartServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private SHInitiative		initiative			= null;
	private String				logfile				= null;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		//System.out.println(">InitiativeStartServlet: " + request.getParameter("action"));

		try {
			if (request.getParameter("action").equals("login")) {
				// Verifying the password and recovering the initiative (if exists)
				String title = (String) request.getParameter("user");
				String pword = (String) request.getParameter("pword");
				System.out.println("Login: " + title + ", " + pword);

				// Recovering and SETTING the initiative to the SESSION.     //
				// ----------------------------------------------------------//
				this.initiative = recoverInitiative(title, pword);           //
				request.getSession().setAttribute("initiative", initiative); //
				// ----------------------------------------------------------//

				if (logfile == null) {
					// setLogOutput(request.getSession().getServletContext().getRealPath("/") + "/SHlogfile.txt");
					setLogOutput(request.getSession().getServletContext().getRealPath("/"));
					request.getSession().setAttribute("logfile", logfile);
					System.out.println("\n### STARTING APPLICATION ###");
					System.out.println("\n# Initiative Identification: "+ initiative);
				}

				//System.out.printf("Initiative: %s, %s, %s, %s\n", initiative.getDomain(), initiative.getPurpose(), initiative.getScope(), initiative.getPeople());
				request.getRequestDispatcher("initiativestarter.jsp").forward(request, response);

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
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
	}

	private SHInitiative recoverInitiative(String title, String pword) {
		// TODO: test pword and recover initiative
		// TODO: move to application
		return new SHInitiative(title, "", "", "", null);
	}

	/* Defines the output log file. */
	private void setLogOutput(String logpath) {
		//logfile = logpath + "/log/SHLog." + new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss").format(new Date()) + ".txt";
		logfile = "log/SHLog." + new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss").format(new Date()) + ".txt";
		PrintStream ps;
		try {
			ps = new PrintStream(logpath + logfile);
			System.setOut(ps);
			System.setErr(ps);
			System.out.println("SH Approach log file - " + new java.util.Date());
			System.out.println("---------------------------------------------------");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}