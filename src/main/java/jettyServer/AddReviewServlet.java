package jettyServer;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Reviews servlet to handle reviews related requests.
 */
@SuppressWarnings("serial")
public class AddReviewServlet extends HttpServlet {
	
	ReviewBaseServlet reviewBaseServlet;
	
	/**
	 * Constructor of this Servlet class
	 */
	public AddReviewServlet() {
		reviewBaseServlet = new ReviewBaseServlet();
	}
	
	/**
	 * Processes GET request related to reviews and sends html response to the client.
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
		
		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = ve.getTemplate("templates/addReview.html");
		context.put("hotelId", hotelId);
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
	 * Processes POST request related to adding/editing reviews and sends html response to the client.
	 * Sets appropriate status headers.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String hotelId = request.getParameter("hotelId");
		String reviewTitle = StringEscapeUtils.escapeHtml4(request.getParameter("title"));
		String reviewText = StringEscapeUtils.escapeHtml4(request.getParameter("reviewText"));
		Boolean isRecom = Boolean.valueOf(StringEscapeUtils.escapeHtml4(request.getParameter("isRecom")));
		String reviewdate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd\'T\'HH:mm:ss\'Z\'")).toString();
		System.out.println(reviewdate);
		
		double rating = 0;
		try {
			rating = Double.parseDouble(StringEscapeUtils.escapeHtml4(request.getParameter("rating")));
		} catch (Exception e) {
			System.err.println("Invalid rating: " + rating);
		}
		
		String user = request.getSession().getAttribute("username").toString();
		if (user.isEmpty()) user = "anonymous";
		long timeSeed = System.nanoTime(); // to get the current date time value
		double randSeed = Math.random() * 1000; // random number generation
		long midSeed = (long) (timeSeed * randSeed); // mixing up the time and
		String s = midSeed + "";
		String reviewid = s.substring(0, 9);
		Status status = reviewBaseServlet.addReview(reviewid, hotelId, user, rating, isRecom, reviewTitle, reviewText, reviewdate);
		
		
		try {
			if (status == Status.OK) {
				response.sendRedirect(response.encodeRedirectURL("/hotelInfo?hotelId=" + hotelId));
			}
			else {
				response.sendRedirect(response.encodeRedirectURL("/addReview?hotelId=" + hotelId + "&error=" + status.ordinal()));
			}
		}
		catch (Exception ex) {
			System.err.println("Unable to process login form."+ ex);
		}
	}
	
}