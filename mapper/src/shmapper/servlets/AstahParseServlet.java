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

import shmapper.applications.AstahParseApp;
import shmapper.applications.ParserException;
import shmapper.model.SHInitiative;

/** Servlet implementation class AstahParseServlet */
@WebServlet("/AstahParseServlet")
public class AstahParseServlet extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private AstahParseApp		parser;
	private String				workingDir			= "";
	private Path				path				= null;
	private String				results				= null;
	private boolean				success				= true;
	private boolean				importable			= true;

	/* doPost method, for processing the upload and calling the parsers. */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// System.out.println(">AstahParseServlet: " + request.getParameter("action"));
		try {
			if (request.getParameter("action") == null) { // No action defined: file upload
				// Uploading File
				uploadAstah(request, response);

			} else if (request.getParameter("action").equals("openPage")) {
				System.out.println("\n# Astah Parsing");
				// Opening page
				request.getRequestDispatcher("astahparser.jsp").forward(request, response);

			} else if (request.getParameter("action").equals("images")) {
				// Importing Images
				importImages(request, response);

			} else {
				System.out.println("No action identified");
			}
		} catch (ServletException | IOException | ParserException e) {
			results += parser.getResults();
			results += "<span style='color:red'>" + e.getMessage().replaceAll("\n", "<br/>") + "</span>";
			results += "<br/><b>Please, fix your astah file and try again.</b>";
			results += "<br/><a id='logfile' href='" + request.getSession().getAttribute("logfile") + "' target='_blank' hidden><code>log file</code></a>";
			success = false;
			e.printStackTrace();
		} finally {
			response.getWriter().print(results);
		}
	}

	/* Gets the uploaded file, saves it, and starts the parsing. */
	private void uploadAstah(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ParserException {
		// Accessing, saving and processing the uploaded astah file.
		try {
			List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
			String filename = null;
			for (FileItem item : items) {
				filename = item.getName();
				InputStream content = item.getInputStream();

				response.setContentType("text/plain");
				response.setCharacterEncoding("UTF-8");

				// Saving file on disk
				workingDir = request.getSession().getServletContext().getRealPath("/");
				path = Paths.get(workingDir + "/Uploaded_" + filename.replaceAll("[^a-zA-Z0-9.-]", "_")); // eliminating
																											// special
																											// chars
				Files.copy(content, path, StandardCopyOption.REPLACE_EXISTING);
			}
			results = "File <i>" + filename + "</i> uploaded for the initiative.<br/>";

			// Initializing the Application and Parsing the Models
			SHInitiative initiative = (SHInitiative) request.getSession().getAttribute("initiative");
			// reseting the initiative (removing all packages and mappings)
			initiative.resetInitiative();
			System.out.println("* Initiative RESET");
			parser = new AstahParseApp(initiative);
			parser.parseAstah(path.toString());
			results += parser.getResults();

		} catch (FileUploadException e) {
			throw new ServletException("Parsing file upload failed.", e);
		}
	}

	/* Imports the images from the uploaded astha file. */
	private void importImages(HttpServletRequest request, HttpServletResponse response) throws IOException, ParserException {
		if (success && importable) {
			results = "";
			parser.importImages(path.toString(), workingDir);
			results += parser.getResults();
			results += "<br/><span style='color:blue'><b>Astah File successfully read and parsed!</span></b><br/>Proceed to the Mapping.";
			success = true;
		}
	}

}