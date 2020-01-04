package jettyServer;

import hotelapp.HotelDetails;

import java.util.List;

/**
 * Hotel servlet to interact with UI servlet and Database to
 * fetch hotel data from DB and use it in UI.
 */
public class HotelBaseServlet {
	protected static final HotelDatabaseHandler dbhandler = HotelDatabaseHandler.getInstance();
	
	/** Get all distinct cities for all hotels */
	protected List<String> getHotelsCities() {
		List<String> cities = dbhandler.getCities();
		return cities;
	}
	
	/** Get hotels based on city and hotel name */
	protected List<HotelDetails> getSearchedHotels(String name, String city) {
		List<HotelDetails> hotels = dbhandler.searchHotels(name, city);
		return hotels;
	}
	
	/** Get hotel details based on hotel id */
	public HotelDetails getHotelById (String id) {
		return dbhandler.getHotel(id);
	}
	
	/** Get all hotels in DB */
	protected List<HotelDetails> getAllHotels() {
		return dbhandler.getAllHotels();
	}
}
