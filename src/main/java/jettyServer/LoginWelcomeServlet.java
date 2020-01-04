package jettyServer;

import hotelapp.HotelDetails;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Handles display of user information.
 */
@SuppressWarnings("serial")
public class LoginWelcomeServlet extends LoginBaseServlet {
	
	/**
	 * Processes GET request related to welcome and sends HTML response to the client.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 * @throws IOException throws exception if any
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		HttpSession session = request.getSession();
		String user = (String) session.getAttribute("username");
		
		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/welcomeUser.html");
		
		
		if (user != null) {
			
			PrintWriter out = response.getWriter();
			context.put("welcomeMsg", "Hello " + user + "!");
			
			HotelBaseServlet hotelBaseServlet = new HotelBaseServlet();
			List<String> cities = hotelBaseServlet.getHotelsCities();
			context.put("cities", cities);
			
			String lastLoginDate = getLastLogin(user);
			context.put("lastLogin", lastLoginDate);
			
			String name = request.getParameter("name");
			String city = request.getParameter("city");
			
			if(name != null || city != null) {
				List<HotelDetails> hotels = hotelBaseServlet.getSearchedHotels(name, city);
				context.put("hotels", hotels);
			}
			StringWriter writer = new StringWriter();
			template.merge(context, writer);
			out.println(writer.toString());
			
		}
		else {
			response.sendRedirect("/login");
		}
	}
	
	/**
	 * Processes POST request related to welcome and sends HTML response to the client.
	 * Calls GET request.
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