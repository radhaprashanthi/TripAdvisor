package jettyServer;

import hotelapp.HotelDetails;
import hotelapp.ThreadSafeHotelData;
import hotelapp.TouristAttraction;
import hotelapp.TouristAttractionFinder;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * Attractions servlet to handle attractions related requests.
 * Uses ThreadSafeHotelData class to fetch attractions based on hotelId and radius.
 */
@SuppressWarnings("serial")
public class AttractionsServlet extends HttpServlet {
	
	private ThreadSafeHotelData hotelData;
	
	/**
	 * Constructor of this Servlet class
	 * @param data ThreadSafeHotelData instance to fetch attractions
	 */
	public AttractionsServlet(ThreadSafeHotelData data) {
		hotelData = data;
	}
	
	/**
	 * Processes GET request related to attractions and sends HTML response to the client.
	 * Sets appropriate status headers.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 * @throws ServletException throws exception if any
	 * @throws IOException throws exception if any
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		String user = (String) session.getAttribute("username");
		
		if(user == null) {
			response.sendRedirect("/login");
		}
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		String hotelId = StringEscapeUtils.escapeHtml4(request.getParameter("hotelId"));
		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/attractions.html");
		
		if (hotelId != null) {
			Integer radius = 2;
			try {
				radius = Integer.parseInt(StringEscapeUtils.escapeHtml4(request.getParameter("radius")));
			} catch (Exception e) {
				System.err.println("Invalid parameter: radius = " + radius);
			}
			
			HotelBaseServlet hbServlet = new HotelBaseServlet();
			HotelDetails hotelDetails = hbServlet.getHotelById(hotelId);
			
			TouristAttractionFinder attractionFinder = new TouristAttractionFinder(hotelData);
			System.out.println("Radius:" + radius);
			attractionFinder.fetchAttractions(hotelId, radius);
			List<TouristAttraction> attractions = hotelData.findAttractionsByHotelID(hotelId);
			if (attractions != null) {
				context.put("name", hotelDetails.getName());
				context.put("radius", radius);
				context.put("attractions", attractions);
				context.put("hotelId", hotelId);
				response.setStatus(HttpServletResponse.SC_OK);
				
			}
		}
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		
		out.println(writer.toString());
	}
	
	/**
	 * Processes POST request related to attractions and sends HTML response to the client.
	 * Sets appropriate status headers.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			doGet(request, response);
		} catch (ServletException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	
}