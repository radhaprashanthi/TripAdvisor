package jettyServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import hotelapp.HotelDetails;
import hotelapp.HotelReview;
import hotelapp.ThreadSafeHotelData;
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
 * Reviews servlet to handle reviews related requests.
 */
@SuppressWarnings("serial")
public class ReviewsServlet extends HttpServlet {
	
	/**
	 * Processes GET request related to reviews and sends HTML response to the client.
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
		
		ReviewBaseServlet reviewBaseServlet = new ReviewBaseServlet();
		
		response.setContentType("text/html");
		
		PrintWriter out = response.getWriter();
		String hotelId = StringEscapeUtils.escapeHtml4(request.getParameter("hotelId"));
		String reviewId = StringEscapeUtils.escapeHtml4(request.getParameter("reviewId"));
		if (reviewId != null) {
			Status status = reviewBaseServlet.deleteReview(reviewId);
			if (status == Status.OK) {
				response.sendRedirect("/reviews?hotelId=" + hotelId);
			}
		}
		
		List<HotelReview> reviews = reviewBaseServlet.getReviewsByHotelId(hotelId);
		
		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/reviews.html");
		if (hotelId != null && reviews != null) {
			//HotelDetails hotelDetails = hotelData.findHotelById(hotelId);
			
			HotelBaseServlet hbServlet = new HotelBaseServlet();
			HotelDetails hotelDetails = hbServlet.getHotelById(hotelId);
			
			context.put("name", hotelDetails.getName());
			context.put("reviews", reviews);
			context.put("hotelId", hotelId);
			context.put("username",user);
		}
		else {
			context.put("name", "Invalid hotel name");
		}
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		
		out.println(writer.toString());
	}
}