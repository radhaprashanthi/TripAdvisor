package jettyServer;

import hotelapp.HotelDetails;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Home servlet to show login and register options.
 */
@SuppressWarnings("serial")
public class HomeServlet extends HttpServlet {

	
	/**
	 * Home servlet to redirect to login and register pages and show google map
	 * Sets appropriate status headers.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 * @throws ServletException throws exception if any
	 * @throws IOException throws exception if any
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		HotelBaseServlet hotelBaseServlet = new HotelBaseServlet();
		response.setContentType("text/html");
		
		PrintWriter out = response.getWriter();
		List<HotelDetails> hotels = hotelBaseServlet.getAllHotels();
		List<List<String>> hotelStrings = new ArrayList<>();
		for (HotelDetails hotel: hotels) {
			List<String> hotelStr = new ArrayList<>();
			hotelStr.add(hotel.getName());
			hotelStr.add(String.valueOf(hotel.getLatitude()));
			hotelStr.add(String.valueOf(hotel.getLongitude()));
			hotelStrings.add(hotelStr);
		}
		System.out.println(hotelStrings);
		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/home.html");
		context.put("hotels", hotelStrings);
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		
		out.println(writer.toString());
		
	}
}