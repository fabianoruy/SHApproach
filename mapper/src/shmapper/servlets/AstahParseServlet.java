package shmapper.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import shmapper.applications.ManagerApp;
import shmapper.model.SHInitiative.InitiativeStatus;
import shmapper.util.ParserException;

/** Servlet implementation class AstahParseServlet */
@WebServlet("/AstahParseServlet")
public class AstahParseServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private ManagerApp			main				= null;
	private Path				path				= null;
	private String				results;
	private boolean				success				= true;

	/* doPost method, for processing the upload and calling the parsers. */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			// Accessing the main app from the Session
			main = (ManagerApp) request.getSession().getAttribute("main");
			results = "";

			if (request.getParameter("action") == null) { // No action defined: file upload
				// Uploading File
				uploadAstah(request, response);

			} else if (request.getParameter("action").equals("startParse")) {
				// Opening page
				main.log.println("\n# Astah Parsing");
				request.getRequestDispatcher("astahparser.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("images")) {
				// Importing Images
				importImages(request, response);

			} else {
				main.log.println("No action identified");
			}
		} catch (Exception e) {
			success = false;
			results += main.getParser().getResults();
			if (e.getMessage() != null) {
				results += "<span style='color:red'>" + e.getMessage().replaceAll("\n", "<br/>") + "</span>";
			}
			results += "<br/><b>Please, fix your astah file and try again.</b>";
			main.log.println("!" + e.getMessage());
			main.log.println("Please, fix your astah file and try again.");

			main.log.println("(!) Parse has failed! Cleaning data.");
			main.log.println("* INITIATIVE RESET");
			main.getInitiative().resetInitiative();
			try {
				main.getInitiative().saveInitiative();
				main.log.println(".");
			} catch (IOException e1) {
				e1.printStackTrace(main.log);
			}
		} finally {
			try {
				response.getWriter().print(results);
				results = "";
				if (!success) {
					response.setStatus(500);
					success = true; // reseting for another try.
				}
				response.flushBuffer();
			} catch (IOException e) {
				e.printStackTrace(main.log);
			}
		}
	}

	/* Gets the uploaded file, saves it, and starts the parsing. */
	private void uploadAstah(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ParserException, FileUploadException {
		// Accessing, saving and processing the uploaded astah file.
		List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
		String fname = null;
		for (FileItem item : items) {
			fname = item.getName();
			InputStream content = item.getInputStream();
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");

			// Saving file on disk
			String workingDir = main.getMapperpath() + main.getInitpath();
			path = Paths.get(workingDir + "/uploaded_" + fname.replaceAll("[^a-zA-Z0-9.-]", "_")); // no special chars
			Files.copy(content, path, StandardCopyOption.REPLACE_EXISTING);
		}
		results = "File <i>" + fname + "</i> uploaded for the initiative.<br/>";

		// RESETING the initiative (removing all packages and mappings, if exists)
		main.log.println("* INITIATIVE RESET");
		main.getInitiative().resetInitiative();

		// Initializing the Application and PARSING the Models
		main.getParser().parseAstah(path.toString().replace("\\", "/"));
		results += main.getParser().getResults();
	}

	/* Imports the images from the uploaded astha file. */
	private void importImages(HttpServletRequest request, HttpServletResponse response) throws IOException, ParserException {
		boolean importable = true;
		if (success) {
			if (importable) {
				main.getParser().importImages(path.toString(), main.getMapperpath() + main.getInitpath());
				results += main.getParser().getResults();
				results += "<br/><span style='color:blue'><b>Astah File successfully read and parsed! \\o/ </span></b><br/>Proceed to the Mappings.<br/>";
				main.log.println("\nAstah File successfully read and parsed! \\o/" + "\nProceed to the Mappings!");
			}
			main.getInitiative().setStatus(InitiativeStatus.PARSED);
			main.getInitiative().saveInitiative();
			main.log.println(".");
		}
	}

}