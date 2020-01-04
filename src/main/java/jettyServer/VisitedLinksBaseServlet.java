package jettyServer;

import java.util.List;

/**
 * Class to interact with Servlets and to fetch data from DB related
 * to expedia links visited by user
 */
public class VisitedLinksBaseServlet {
	/** Instance of visited links DB handler */
	protected static final VisitedLinksDatabaseHandler dbhandler = VisitedLinksDatabaseHandler.getInstance();
	
	/**
	 * Saves link to DB by hotel id and username
	 * @param id - hotel id visited by user
	 * @param user - User who visited the expedia link
	 * @return Status OK if successfully saved to DB
	 */
	protected Status saveLink(String id, String user) {
		return dbhandler.saveLink(id, user);
	}
	
	/**
	 * Fetches all the links visited by user
	 * @param user - username
	 * @return List of hotel id's visited by user
	 */
	protected List<String> getLinksVisitedByUser(String user) {
		return dbhandler.getLinksVisitedByUser(user);
	}
	
	/**
	 * Delete visited link by user
	 * @param id - link id
	 * @param user - user name
	 * @return Status OK if link is deleted
	 */
	protected Status deleteVisitedLink (String id, String user) {
		return dbhandler.removeVisitedLink(id, user);
	}
	
	/**
	 * Check if link is visited
	 * @param id - link id
	 * @param user - user name
	 * @return true if link exists
	 */
	protected boolean isLinkVisited (String id, String user) {
		Status status = dbhandler.checkIfLinkIsVisited(id, user);
		if (status == Status.DUPLICATE_LINK) {
			return true;
		}
		return false;
	}
	
	/**
	 * Remove all links for a user. Used in profile tab to clear all visited links history.
	 * @param user - user name
	 * @return Status OK if all links are deleted
	 */
	protected Status removeAllLinksVisitedByUser(String user) {
		return dbhandler.clearAllLinksVisitedByUser(user);
	}
}
