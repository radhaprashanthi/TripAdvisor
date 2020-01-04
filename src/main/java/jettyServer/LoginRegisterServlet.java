package jettyServer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Handles user registration servlet
 */
@SuppressWarnings("serial")
public class LoginRegisterServlet extends LoginBaseServlet {
	
	/**
	 * Processes GET request related to registration and sends HTML response to the client.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 * @throws IOException throws exception if any
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");
		
		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/registerUser.html");
		
		
		if(error != null) {
			String errorMessage = getStatusMessage(error);
			context.put("errorMessage", errorMessage);
		}
		
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		
		out.println(writer.toString());
	}
	
	/**
	 * Processes POST request related to registration and sends HTML response to the client
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 * @throws IOException throws exception if any
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		String newuser = request.getParameter("user");
		String newpass = request.getParameter("pwd");
		
		Status status = dbhandler.registerUser(newuser, newpass);

		if(status == Status.OK) {
			response.sendRedirect(response.encodeRedirectURL("/login?newuser=true"));
		}
		else {
			String url = "/register?error=" + status.name();
			url = response.encodeRedirectURL(url);
			response.sendRedirect(url);
		}
	}
}