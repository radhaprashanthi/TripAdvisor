package jettyServer;

import hotelapp.HotelDetails;
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
 * Used to save hotels and visited Expedia links
 */
@SuppressWarnings("serial")
public class AddFavouritesServlet extends HttpServlet {
	/**
	 * Processes GET request related to saving hotel to favourites and storing
	 * Expedia links visited by user.
	 * Sets appropriate status headers.
	 * @param request request read from client's input stream
	 * @param response response written to client's output stream
	 * @throws ServletException throws exception if any
	 * @throws IOException throws exception if any
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		SavedHotelsBaseServlet savedHotelsBaseServlet = new SavedHotelsBaseServlet();
		VisitedLinksBaseServlet visitedLinksBaseServlet = new VisitedLinksBaseServlet();
		
		HttpSession session = request.getSession();
		String user = (String) session.getAttribute("username");
		
		response.setContentType("text/html");
		Status saveStatus = null;
		
		PrintWriter out = response.getWriter();
		String hotelId = StringEscapeUtils.escapeHtml4(request.getParameter("hotelId"));
		String save = StringEscapeUtils.escapeHtml4(request.getParameter("save"));
		String visited = StringEscapeUtils.escapeHtml4(request.getParameter("visited"));
		
		HotelBaseServlet hbServlet = new HotelBaseServlet();
		HotelDetails hotelDetails = hbServlet.getHotelById(hotelId);
		VelocityEngine ve = (VelocityEngine) request.getServletContext().getAttribute("templateEngine");
		VelocityContext context = new VelocityContext();
		Template template = null;
		
		if (user != null) {
			if (save != null) {
				if (save.equals("true") && user != null) {
					template = ve.getTemplate("templates/savedHotelMsg.html");
					saveStatus = savedHotelsBaseServlet.saveHotel(hotelId, user);
					if (saveStatus == Status.OK)
						context.put("savedMsg", "Successfully saved the hotel to favourites");
					else
						context.put("savedMsg", saveStatus.message());
				}
			}
			
			Status visitedStatus = null;
			if (visited != null) {
				if (visited.equals("true") && user != null) {
					template = ve.getTemplate("templates/savedLinkMsg.html");
					visitedStatus = visitedLinksBaseServlet.saveLink(hotelId, user);
					
					if (visitedStatus == Status.OK)
						context.put("visitedMsg", "Link added to history");
					else
						context.put("visitedMsg", visitedStatus.message());
				}
			}
		}
		
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		out.println(writer.toString());
		
	}
}
