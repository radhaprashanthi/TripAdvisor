package jettyServer;

import java.util.List;

/**
 * Saved hotel servlet to interact with UI servlet and Database to
 * fetch saved hotel data from DB and use it in UI.
 */
public class SavedHotelsBaseServlet {
	/** Saved hotel database handler to interact with DB */
	protected static final SavedHotelsDatabaseHandler dbhandler = SavedHotelsDatabaseHandler.getInstance();
	
	/**
	 * Save hotel to DB
	 * @param id - hotel id
	 * @param user - user name
	 * @return status OK if successfully saved
	 */
	protected Status saveHotel(String id, String user) {
		return dbhandler.saveHotel(id, user);
	}
	
	/**
	 * Get all hotels saved by user
	 * @param user - user name
	 * @return saved hotels
	 */
	protected List<String> getSavedHotels(String user) {
		return dbhandler.getSavedHotelsForUser(user);
	}
	
	/**
	 * Delete saved hotel
	 * @param id - hotel id
	 * @param user - user name
	 * @return status OK if hotel succesfully deleted
	 */
	protected Status deleteSavedHotel (String id, String user) {
		return dbhandler.removeSavedHotel(id, user);
	}
	
	/**
	 * Check is hotel saved to DB
	 * @param id - hotel id
	 * @param user - user name
	 * @return true is hotel exists else false
	 */
	protected boolean isHotelSaved (String id, String user) {
		Status status = dbhandler.checkSavedHotelExists(id, user);
		if (status == Status.DUPLICATE_SAVEHOTEL) {
			return true;
		}
		return false;
	}
	
	/**
	 * Removes all hotels saved by user.Used to clear list in profile
	 * @param user - user name
	 * @return status OK if all hotels are removed successfully
	 */
	protected Status removeAllHotelsByUser(String user) {
		return dbhandler.removeAllSavedHotelsByUser(user);
	}
}
