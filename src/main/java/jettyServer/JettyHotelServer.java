package jettyServer;

import hotelapp.ThreadSafeHotelData;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.logging.Handler;

/**
 * This class uses Jetty & servlets to implement server serving hotel info, reviews and attractions
 * Also handles other requests like login, welcome, logout, register,
 * search, adding/editing/deleting reviews, profile
 */
public class JettyHotelServer {
    // FILL IN CODE
    public final static int PORT = 8081;
	
	/**
	 * Driver method of this class.
	 * Pre-loads thread safe hotel data.
	 * Maps servlets with handlers.
	 * Starts Jetty server.
	 * @param args takes command line arguments -hotel hotelpath -reviews reviewsdir
	 * @throws Exception throws exceptions is any
	 */
	public static void main(String[] args) {
		
		Server server = new Server(PORT);
		//ThreadSafeHotelData data = new HotelSearch().loadHotelData(args);
		ThreadSafeHotelData data = new ThreadSafeHotelData();
		
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.addServlet(HomeServlet.class, "/home");
		context.addServlet(new ServletHolder(new HotelInfoServlet()), "/hotelInfo");
		context.addServlet(new ServletHolder(new ReviewsServlet()), "/reviews");
		context.addServlet(new ServletHolder(new AttractionsServlet(data)), "/attractions");
		
		context.addServlet(new ServletHolder(new LoginUserServlet()),     "/login");
		context.addServlet(new ServletHolder(new LoginRegisterServlet()), "/register");
		context.addServlet(new ServletHolder(new LoginWelcomeServlet()),  "/welcome");
		context.addServlet(new ServletHolder(new LoginRedirectServlet()), "/*");
		
		context.addServlet(AddReviewServlet.class, "/addReview");
		context.addServlet(EditReviewServlet.class, "/editReview");
		context.addServlet(ProfileServlet.class,  "/profile");
		
		context.addServlet(HotelSearchServlet.class,  "/hotelSearch");
		context.addServlet(AddFavouritesServlet.class,  "/addFavourites");
		
		// initialize Velocity
		VelocityEngine velocity = new VelocityEngine();
		velocity.init();
		
		// set velocity as an attribute of the context so that we can access it
		// from servlets
		context.setContextPath("/");
		context.setAttribute("templateEngine", velocity);
		server.setHandler(context);
		
		/*ResourceHandler resource_handler = new ResourceHandler(); // a handler for serving static pages
		resource_handler.setDirectoriesListed(true);
		
		resource_handler.setResourceBase("static");
		
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resource_handler, context });
		server.setHandler(handlers);
		
		 */
		
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
