package jettyServer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Redirects to welcome page or login page depending on whether user
 * session is detected.
 */
@SuppressWarnings("serial")
public class LoginRedirectServlet extends LoginBaseServlet {
	
	/**
	 * Processes GET request related to login and sends HTML response to the client.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 * @throws IOException throws exception if any
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		HttpSession session = request.getSession();
		String user = (String) session.getAttribute("username");
		
		if(user == null) {
			response.sendRedirect("/login");
		}
		
		else {
			response.sendRedirect("/welcome");
		}
	}
	
	/**
	 * Processes POST request related to login and sends HTML response to the client.
	 * and redirects to GET
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 * @throws IOException throws exception if any
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doGet(request, response);
	}
}