package shmatcher.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import shmatcher.model.SHInitiative;
import shmatcher.model.SimpleMatch;

/* Servlet implementation class MatcherServlet */
@WebServlet("/MatcherServlet")
public class MatcherServlet extends HttpServlet {
	private static final long			serialVersionUID	= 1L;
	private static List<SimpleMatch>	allMatches			= new ArrayList<SimpleMatch>();
	private SHInitiative				initiative;

	/* HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response). */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Accessing the initiative from the Session
		initiative = (SHInitiative) request.getSession().getAttribute("initiative");
		System.out.println("Initiative: " + initiative.getDomain());

		if (request.getParameter("action").equals("match")) {
			String elem = request.getParameter("elem");
			String conc = request.getParameter("conc");
			String cover = request.getParameter("cover");
			String comm = request.getParameter("comm");

			SimpleMatch match = new SimpleMatch(elem, conc, cover, comm);
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

}
