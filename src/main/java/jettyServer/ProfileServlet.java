package jettyServer;

import hotelapp.HotelDetails;
import hotelapp.HotelReview;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Handles display of user profile.
 */
@SuppressWarnings("serial")
public class ProfileServlet extends HttpServlet {
	
	/**
	 * Processes GET request related to profile like saving hotels, saving visited links,
	 * deleting reviews and sends HTML response to the client.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 * @throws IOException throws exception if any
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		HttpSession session = request.getSession();
		String user = (String) session.getAttribute("username");
		
		SavedHotelsBaseServlet savedHotelsBaseServlet = new SavedHotelsBaseServlet();
		VisitedLinksBaseServlet visitedLinksBaseServlet = new VisitedLinksBaseServlet();
		ReviewBaseServlet reviewBaseServlet = new ReviewBaseServlet();
		
		String hotelId = request.getParameter("hotelId");
		String savedHotel = request.getParameter("savedHotel");
		String visitedLink = request.getParameter("visitedLink");
		String reviewId = request.getParameter("reviewId");
		String deleteReview = request.getParameter("deleteReview");
		
		String clearSavedHotels = request.getParameter("clearSavedHotels");
		String clearVisitedLinks = request.getParameter("clearVisitedLinks");
		String clearReviews = request.getParameter("clearReviews");
		
		if(user != null) {
			if (clearSavedHotels != null) {
				if (clearSavedHotels.equals("true")) {
					Status s = savedHotelsBaseServlet.removeAllHotelsByUser(user);
					if (s == Status.OK)
						response.sendRedirect("/profile");
				}
			}
			
			if (clearVisitedLinks != null) {
				if (clearVisitedLinks.equals("true")) {
					Status s = visitedLinksBaseServlet.removeAllLinksVisitedByUser(user);
					if (s == Status.OK)
						response.sendRedirect("/profile");
				}
			}
			
			if (clearReviews != null) {
				if (clearReviews.equals("true")) {
					Status s = reviewBaseServlet.deleteAllReviewsByUser(user);
					if (s == Status.OK)
						response.sendRedirect("/profile");
				}
			}
			
			if (hotelId != null && savedHotel != null) {
				if (savedHotel.equals("true")) {
					Status s = savedHotelsBaseServlet.deleteSavedHotel(hotelId, user);
					if (s == Status.OK)
						response.sendRedirect("/profile");
				}
			}
			
			if (hotelId != null && visitedLink != null) {
				if (visitedLink.equals("true")) {
					Status s = visitedLinksBaseServlet.deleteVisitedLink(hotelId, user);
					if (s == Status.OK)
						response.sendRedirect("/profile");
				}
			}
			
			if (reviewId != null && deleteReview != null) {
				if (deleteReview.equals("true")) {
					Status s = reviewBaseServlet.deleteReview(reviewId);
					if (s == Status.OK)
						response.sendRedirect("/profile");
				}
			}
			
			VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
			VelocityContext context = new VelocityContext();
			Template template = ve.getTemplate("templates/userProfile.html");
			
			
			PrintWriter out = response.getWriter();
			context.put("user", user);
			List<String> hotelIds = savedHotelsBaseServlet.getSavedHotels(user);
			HotelBaseServlet hotelBaseServlet = new HotelBaseServlet();
			List<HotelDetails> savedHotels = new ArrayList<>();
			if (hotelIds != null) {
				for (String id : hotelIds)
					savedHotels.add(hotelBaseServlet.getHotelById(id));
			}
			context.put("savedHotels", savedHotels);
			
			List<String> visitedLinks = visitedLinksBaseServlet.getLinksVisitedByUser(user);
			context.put("visitedLinks", visitedLinks);
			
			List<HotelReview> reviews = reviewBaseServlet.getReviewsByUser(user);
			context.put("reviews", reviews);
			
			StringWriter writer = new StringWriter();
			template.merge(context, writer);
			
			out.println(writer.toString());
			
		}
		else {
			response.sendRedirect("/login");
		}
	}
	
	/**
	 * Calls GET request
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