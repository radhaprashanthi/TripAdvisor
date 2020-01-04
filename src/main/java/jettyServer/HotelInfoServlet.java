package jettyServer;

import hotelapp.HotelDetails;
import hotelapp.ThreadSafeHotelData;
import hotelapp.TouristAttractionFinder;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * HotelInfo servlet to handle hotel info related requests.
 */
@SuppressWarnings("serial")
public class HotelInfoServlet extends HttpServlet {
	
	/**
	 * Processes GET request related to hotel info and sends HTML response to the client.
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
		
		HotelBaseServlet hbServlet = new HotelBaseServlet();
		HotelDetails hotelDetails = hbServlet.getHotelById(hotelId);
		
		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/hotelInfo.html");
		if (hotelDetails != null) {
			context.put("name", hotelDetails.getName());
			context.put("hotelId", hotelDetails.getId());
			context.put("addr", hotelDetails.getStreet());
			context.put("city", hotelDetails.getCity());
			context.put("state", hotelDetails.getState());
			context.put("hotelname", hotelDetails.getName());
			context.put("lat", hotelDetails.getLatitude());
			context.put("lng", hotelDetails.getLongitude());
			System.out.println(hotelDetails.getLatitude()+ " " + hotelDetails.getLongitude());
		}
		else {
			context.put("name", "Invalid hotel name");
		}
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		
		out.println(writer.toString());
		
	}
	
	
}