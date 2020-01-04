package hotelapp;

/** This class stores hotel details.
 *  Used while parsing "hotels200.json" file that contains info about hotels.
 */

public class HotelDetails{
	
	private String id;
	private String name;
	private String street;
	private String city;
	private String state;
	private double latitude;
	private double longitude;
	private String areaDescription;
	private String propertyDescription;
	private String avgRating;
	
	/**
	 * Constructor of this class
	 * @param id - id of a hotel
	 * @param name - name of the hotel
	 * @param street - street where the hotel is located
	 * @param city - city of the hotel
	 * @param state - state of the hotel
	 * @param latitude - latitude info about the hotel
	 * @param longitude - longitude info about the hotel
	 */
	public HotelDetails(String id, String name, String street, String city, String state, double latitude, double longitude) {
		this.id = id;
		this.name = name;
		this.city = city;
		this.state = state;
		this.street = street;
		this.latitude =latitude;
		this.longitude = longitude;
	}
	
	public HotelDetails(String id, String name, String street, String city, String state) {
		this.id = id;
		this.name = name;
		this.street = street;
		this.city = city;
		this.state = state;
	}
	
	public HotelDetails (String name, double latitude, double longitude) {
		this.name = name;
		this.latitude =latitude;
		this.longitude = longitude;
	}
	
	/**
	 * Returns the name of the hotel.
	 * @return -returns hotel name
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Sets area description for the hotel
	 * @param areaDescription  has city where the hotel is located in and the area description
	 */
	public void setAreaDescription(String areaDescription) {
		this.areaDescription = areaDescription;
	}
	
	/**
	 * Sets property description of the hotel
	 * @param propertyDescription Has hotel name and property description
	 */
	public void setPropertyDescription(String propertyDescription) {
		this.propertyDescription = propertyDescription;
	}
	
	/**
	 * Returns hotel id
	 * @return Id of the hotel
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * Returns street of the hotel
	 * @return Street where the hotel is in
	 */
	public String getStreet() {
		return this.street;
	}
	
	/**
	 * Returns the State where the hotel is located
	 * @return State of hotel
	 */
	public String getState() {
		return this.state;
	}
	
	/**
	 * Returns city of the hotel
	 * @return city where hotel is located
	 */
	public String getCity() {
		return this.city;
	}
	
	/**
	 * Returns Latitude location of the hotel
	 * @return latitude co-ordinate of the hotel
	 */
	public double getLatitude() {
		return this.latitude;
	}
	
	/**
	 * Returns longitude location of the hotel
	 * @return longitude co-ordinate of the hotel
	 */
	public double getLongitude() {
		return this.longitude;
	}
	
	/**
	 * Returns area description of the hotel
	 * @return hotel area description
	 */
	public String getAreaDescription() {
		return this.areaDescription;
	}
	
	/**
	 * Returns property description of the hotel
	 * @return hotel property description
	 */
	public String getPropertyDescription() {
		return this.propertyDescription;
	}
	
	/** Get average rating for a hotel*/
	public String getAvgRating() {
		return avgRating;
	}
	
	/** Set average rating of hotel*/
	public void setAvgRating(String avgRating) {
		this.avgRating = avgRating;
	}
	
	/**
	 * Returns hotel information objects
	 * @return - string of hotel information.
	 */
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.name);
		sb.append(": ");
		sb.append(this.id);
		sb.append(System.lineSeparator());
		sb.append(this.street);
		sb.append(System.lineSeparator());
		sb.append(this.city);
		sb.append(", ");
		sb.append(this.state);
		sb.append(System.lineSeparator());
		
		return sb.toString();
	}
}
