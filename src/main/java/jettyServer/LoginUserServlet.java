package jettyServer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Handles login requests.
 */
@SuppressWarnings("serial")
public class LoginUserServlet extends LoginBaseServlet {
	
	/**
	 * Processes GET request related to login and sends HTML response to the client.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 * @throws IOException throws exception if any
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");
		int code = 0;
		
		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/loginUser.html");
		
		if (error != null) {
			try {
				code = Integer.parseInt(error);
			}
			catch (Exception ex) {
				code = -1;
			}

			String errorMessage = getStatusMessage(code);
			context.put("errorMessage", errorMessage);
		}

		if (request.getParameter("newuser") != null) {
			String successMessage = "<p>Registration was successful!" + System.lineSeparator() + "Login with your new username and password below.</p>";
			context.put("successMessage", successMessage);
		}

		if (request.getParameter("logout") != null) {
			context.put("logoutMsg", "<p>Successfully logged out.</p>");
		}
		
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		
		out.println(writer.toString());
	}
	
	
	/**
	 * Processes POST request related to login and sends HTML response to the client.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 * @throws IOException throws exception if any
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String user = request.getParameter("user");
		String pass = request.getParameter("pwd");
		HttpSession session = request.getSession();
		
		Status status = dbhandler.authenticateUser(user, pass);

		try {
			if (status == Status.OK) {
				session.setAttribute("username", user);
				status = updateLastLogin(getDate(), user);
				response.sendRedirect(response.encodeRedirectURL("/welcome"));
			}
			else {
				session.setAttribute("username","");
				response.sendRedirect(response.encodeRedirectURL("/login?error=" + status.ordinal()));
			}
		}
		catch (Exception ex) {
			System.err.println("Unable to process login form." + " " + ex);
		}
	}
	
}