package shmapper.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

import shmapper.applications.AstahParserApp;
import shmapper.applications.ParserException;

/** Servlet implementation class AstahUploadServlet */
@WebServlet("/AstahUploadServlet")
public class AstahUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AstahParserApp parser;
    private String workingDir = "";
    private Path path = null;

    /* doPost method, for processing the upload and calling the parsers. */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

	// Redirects to the action
	if (request.getParameter("action") == null) { // No action defined: file upload
	    System.out.println("\n\n@@@ STARTING APPLICATION @@@ - " + new Date());
	    System.out.println(">AstahUploadServlet: Upload Astah");
	    uploadAstah(request, response);

	} else if (request.getParameter("action").equals("images")) {
	    System.out.println(">AstahUploadServlet: Import Images");
	    parser.importImages(path.toString(), workingDir);
	    String results = parser.getResults();
	    //TODO: the case where an error has occurred before or during image importing.
	    results += "<br/><span style='color:blue'><b>Astah File successfully read and parsed!</span></b><br/>Please, proceed to the Mapping.";
	    response.getWriter().print(results);
	} else {
	    System.out.println("No action identified");
	}
    }

    /* Gets the uploaded file, saves it, and starts the parsing. */
    private void uploadAstah(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	String ajaxResults = null;
	// Processing the uploaded astah file.
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
		path = Paths.get(workingDir + "/Uploaded_" + filename);
		Files.copy(content, path, StandardCopyOption.REPLACE_EXISTING);
	    }
	    ajaxResults = "File <i>" + filename + "</i> uploaded for the initiative.<br/>";

	    // Initializing the Application and Parsing the Models
	    parser = new AstahParserApp();
	    parser.parseAstah(path.toString());
	    ajaxResults += parser.getResults();

	    // Setting the initiative to the session.
	    request.getSession().setAttribute("initiative", parser.getInitiative());

	} catch (FileUploadException e) {
	    throw new ServletException("Parsing file upload failed.", e);
	} catch (ParserException e) {
	    ajaxResults += "<span style='color:red'>" + e.getMessage().replaceAll("\n", "<br/>") + "</span>";
	    ajaxResults += "<br/><b>Please, fix your astah file and try again.</b>";
	    System.out.println("-> Parse Exception");
	} finally {
	    // Sending answer for the page
	    response.getWriter().print(ajaxResults);
	}

    }

}