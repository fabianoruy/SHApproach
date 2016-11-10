package shmatcher.servlets;

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

import shmatcher.applications.AstahParserApp;

/** Servlet implementation class AstahUploadServlet */
@WebServlet("/AstahUploadServlet")
public class AstahUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AstahParserApp parser;
    private String ajaxResults;

    /* doPost method, for processing the upload and calling the parsers. */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	// Processing the uploaded astah file.
	String domain = null;
	String filename = null;
	String workingDir = "";
	Path path = null;
	try {
	    List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);

	    for (FileItem item : items) {
		if (item.isFormField()) {
		    domain = item.getString();
		} else {
		    filename = item.getName();
		    InputStream content = item.getInputStream();

		    response.setContentType("text/plain");
		    response.setCharacterEncoding("UTF-8");

		    // Saving file on disk

		    workingDir = request.getSession().getServletContext().getRealPath("/");
		    //path = Paths.get(System.getProperty("user.dir").replace('\\', '/') + "/Uploaded_" + filename);
		    path = Paths.get(workingDir + "/Uploaded_" + filename);
		    System.out.println("Filename: " + filename);
		    System.out.println("WorkingDir: " + workingDir);
		    System.out.println("Path: " + path.toAbsolutePath().toString());

		    Files.copy(content, path, StandardCopyOption.REPLACE_EXISTING);
		}
	    }
	} catch (FileUploadException e) {
	    throw new ServletException("Parsing file upload failed.", e);
	}

	ajaxResults = "File <code>" + filename + "</code> uploaded for the initiative.<br/>";

	// Initializing the Application and Parsing the Models
	parser = new AstahParserApp();
	parser.parseAstah(path.toString());
	//parser.importImages(path.toString(), workingDir);
	ajaxResults += parser.getResults();

	// Setting the initiative to the session.
	request.getSession().setAttribute("initiative", parser.getInitiative());

	// Sending answer for the page
	response.getWriter().print(ajaxResults);
    }

}