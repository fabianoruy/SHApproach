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

import shmatcher.services.AstahParser;

/** Servlet implementation class AstahUploader */
@WebServlet("/AstahUploader")
public class AstahUploader extends HttpServlet {
	private static final long	serialVersionUID	= 1L;
	private AstahParser			parser				= new AstahParser();
	private String				ajaxResults;

	/* doPost method, for processing the upload and calling the parsers. */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Processing the uploaded astah file.
		String domain = null;
		String filename = null;
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
					System.out.println(filename);
					// path = Paths.get("C:/Users/Fabiano/workspace/matcher/ASTAH_" + filename);
					path = Paths.get(System.getProperty("user.dir").replace('\\', '/') + "/Uploaded_" + filename);
					Files.copy(content, path, StandardCopyOption.REPLACE_EXISTING);
					System.out.println("#Path: " + path.toAbsolutePath().toString());
				}
			}
		} catch (FileUploadException e) {
			throw new ServletException("Parsing file upload failed.", e);
		}

		ajaxResults = "File <code>" + filename + "</code> uploaded for the initiative <b>" + domain + "</b>.<br/>";

		// Parsing Models
		ajaxResults += parser.parseAstah(path.toString());

		// Sending answer for the page
		response.getWriter().print(ajaxResults);
	}

}