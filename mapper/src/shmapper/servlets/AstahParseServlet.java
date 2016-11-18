package shmapper.servlets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.apache.taglibs.standard.lang.jstl.parser.ParseException;

import shmapper.applications.AstahParseApp;
import shmapper.applications.ParserException;

/** Servlet implementation class AstahParseServlet */
@WebServlet("/AstahParseServlet")
public class AstahParseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private AstahParseApp parser;
	private String workingDir = "";
	private Path path = null;
	private String logfile = null;
	private boolean success = true;
	private boolean importable = true;

	/* doPost method, for processing the upload and calling the parsers. */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//System.out.println(">AstahParseServlet: " + request.getParameter("action"));
		
		if (request.getParameter("action") == null) { // No action defined: file upload
			//setLogOutput(request.getSession().getServletContext().getRealPath("/") + "/SHlogfile.txt");
			logfile = "log/SHLog." + new SimpleDateFormat("yyyy-MM-dd.hh-mm-ss").format(new Date()) + ".txt";
			setLogOutput(request.getSession().getServletContext().getRealPath("/") + File.separator + logfile);
			//request.getSession().setAttribute("logfile", logfile);

			// Uploading File
			System.out.println("\n### STARTING APPLICATION ### - " + new Date());
			uploadAstah(request, response);

		} else if (request.getParameter("action").equals("openPage")) {
			request.getRequestDispatcher("astahparser.jsp").forward(request, response);

		} else if (request.getParameter("action").equals("images")) {
			// Importing Images
			if (success) {
				importImages(request, response);
			}
		} else {
			System.out.println("No action identified");
		}
	}

	/* Gets the uploaded file, saves it, and starts the parsing. */
	private void uploadAstah(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Accessing, saving and processing the uploaded astah file.
		String results = null;
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
				path = Paths.get(workingDir + "/Uploaded_" + filename.replaceAll("[^a-zA-Z0-9.-]", "_")); // eliminating special chars
				Files.copy(content, path, StandardCopyOption.REPLACE_EXISTING);
			}
			results = "File <i>" + filename + "</i> uploaded for the initiative.<br/>";

			// Initializing the Application and Parsing the Models
			parser = new AstahParseApp();
			parser.parseAstah(path.toString());
			results += parser.getResults();

			// Setting the initiative to the session.
			//request.getSession().setAttribute("initiative", parser.getInitiative());

		} catch (FileUploadException e) {
			throw new ServletException("Parsing file upload failed.", e);
		} catch (ParserException e) {
			results += "<span style='color:red'>" + e.getMessage().replaceAll("\n", "<br/>") + "</span>";
			results += "<br/><b>Please, fix your astah file and try again.</b>";
			results += "<br/><a id='logfile' href='" + logfile + "' target='_blank' hidden><code>log file</code></a>";
			success = false;
			System.out.println("-> Parse Exception: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Sending answer for the page
			response.getWriter().print(results);
		}
	}

	/* Imports the images from the uploaded astha file. */
	private void importImages(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String results = "";
		if (importable) {
			try {
				parser.importImages(path.toString(), workingDir);
				results += parser.getResults();
				results += "<br/><span style='color:blue'><b>Astah File successfully read and parsed!</span></b><br/>Proceed to the Mapping.";
			} catch (ParseException e) {
				results += "<span style='color:red'>" + e.getMessage().replaceAll("\n", "<br/>") + "</span>";
				results += "<br/><b>Please, fix your astah file and try again.</b>";
				System.out.println("-> Parse Exception: " + e.getMessage());
				e.printStackTrace();
			} finally {
				results += "<br/><a id='logfile' href='" + logfile
						+ "' target='_blank' hidden><code>log file</code></a>";
				response.getWriter().print(results);
			}
		}
	}

	/* Defines the output log file. */
	private void setLogOutput(String logfile) {
		PrintStream ps;
		try {
			ps = new PrintStream(logfile);
			System.setOut(ps);
			System.setErr(ps);
			System.out.println("SH Approach log file - " + new java.util.Date());
			System.out.println("---------------------------------------------------");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}