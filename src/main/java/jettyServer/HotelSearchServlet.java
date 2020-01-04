package jettyServer;

import hotelapp.HotelDetails;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Class used to search hotels by hotel name keyword and city.
 * Called from Ajax.
 */
@SuppressWarnings("serial")
public class HotelSearchServlet extends HttpServlet {
	
	/**
	 * Processes GET request related to hotel search and sends HTML response to the client.
	 * Redirects to login if not logged in.
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
		Template template = ve.getTemplate("templates/hotelSearchResults.html");
		
		
		if (user != null) {
			
			PrintWriter out = response.getWriter();
			
			HotelBaseServlet hotelBaseServlet = new HotelBaseServlet();
			
			String name = request.getParameter("name");
			String city = request.getParameter("city");
			System.out.println(name+" "+ city);
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
}
