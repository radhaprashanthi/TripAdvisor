package jettyServer;

import hotelapp.HotelReview;
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

/**
 * Reviews servlet to handle editing reviews related requests.
 */
@SuppressWarnings("serial")
public class EditReviewServlet extends HttpServlet {
	
	ReviewBaseServlet reviewBaseServlet;
	
	/**
	 * Constructor of this Servlet class
	 */
	public EditReviewServlet() {
		reviewBaseServlet = new ReviewBaseServlet();
	}
	
	/**
	 * Processes GET request related to editing reviews and sends HTML response to the client.
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
		String error = request.getParameter("error");
		String reviewlId = StringEscapeUtils.escapeHtml4(request.getParameter("reviewId"));
		
		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/editReview.html");
		context.put("hotelId", hotelId);
		context.put("reviewId", reviewlId);
		
		HotelReview review = reviewBaseServlet.getReviewByReviewId(reviewlId);
		if (review != null) {
			context.put("title", review.getTitle());
			context.put("reviewText", review.getReviewText());
			context.put("rating", review.getRating());
			context.put("isRecom", review.isRecommended());
			context.put("userNickName", review.getUserNickname());
		}
		int code = 0;
		
		if (error != null) {
			try {
				code = Integer.parseInt(error);
			}
			catch (Exception ex) {
				code = -1;
			}
			
			String errorMessage = reviewBaseServlet.getStatusMessage(code);
			context.put("errorMessage", errorMessage);
		}
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		
		out.println(writer.toString());
	}
	
	/**
	 * Processes POST request related to editing reviews and sends HTML response to the client.
	 * Sets appropriate status headers.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String hotelId = request.getParameter("hotelId");
		String reviewId = request.getParameter("reviewId");
		String reviewTitle = StringEscapeUtils.escapeHtml4(request.getParameter("title"));
		String reviewText = StringEscapeUtils.escapeHtml4(request.getParameter("reviewText"));
		Boolean isRecom = Boolean.valueOf(StringEscapeUtils.escapeHtml4(request.getParameter("isRecom")));
		
		double rating = 0;
		try {
			rating = Double.parseDouble(StringEscapeUtils.escapeHtml4(request.getParameter("rating")));
		} catch (Exception e) {
			System.err.println("Invalid rating: " + rating);
		}
		
		Status status = reviewBaseServlet.updateReview(reviewId, rating, isRecom, reviewTitle, reviewText);
		
		try {
			if (status == Status.OK) {
				response.sendRedirect(response.encodeRedirectURL("/reviews?hotelId="+hotelId));
			} else {
				response.sendRedirect(response.encodeRedirectURL("/editReview?hotelId=" + hotelId + "&reviewId=" + reviewId + "&error=" + status.ordinal()));
			}
		} catch (Exception ex) {
			System.err.println("Unable to process login form."+ ex);
		}
	}
	
}