package hotelapp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Uses ReentrantReadWriteLock class from the concurrent package.
 * Creates lock of ReentrantReadWriteLock class.
 * Inherits HotelData class and overrides it's methods.
 * Makes the methods thread safe by using read and write locks.
 */
public class ThreadSafeHotelData extends HotelData {
	
	private ReentrantReadWriteLock lock;
	/**
	 * This class constructor calling HotelData (parent) class constructor
	 */
	public ThreadSafeHotelData() {
		super();
		lock = new ReentrantReadWriteLock();
	}
	
	/**
	 * Overridden method to make it thread safe using write lock. Adds hotel to hotelsMap. Similar to project 4.
	 * @param hotelId - id of the hotel
	 * @param hotelName - name of the hotel
	 * @param city - city where the hotel is located
	 * @param state - state where the hotel is
	 * @param streetAddress - street address of the hotel
	 * @param lat - latitude information about hotel
	 * @param lon - longitude information about hotel
	 */
	@Override
	public void addHotel(String hotelId, String hotelName, String city, String state, String streetAddress, double lat,
	                     double lon) {
		try {
		lock.writeLock().lock();
		super.addHotel(hotelId, hotelName, city, state, streetAddress, lat, lon);
		} finally{
		lock.writeLock().unlock();
		}
	}
	
	/**
	 * Overridden method from HotelData to make it thread safe.
	 * @param hotelId
	 *              - the id of the hotel reviewed
	 * @param reviewId
	 *              - the id of the review
	 * @param rating
	 *              - integer rating 1-5.
	 * @param reviewTitle
	 *              - the title of the review
	 * @param review
	 *              - text of the review
	 * @param isRecom
	 *              - whether the user recommends it or not
	 * @param date
	 *              - date of the review
	 * @param username
	 *              - the nickname of the user writing the review.
	 * @return
	 */
	@Override
	public boolean addReview(String hotelId, String reviewId, int rating, String reviewTitle, String review,
	                         boolean isRecom, String date, String username) {
		
		try {
			lock.writeLock().lock();
			boolean isReviewAdded = super.addReview(hotelId, reviewId, rating, reviewTitle, review, isRecom, date, username);
			if(isReviewAdded)
				combine(hotelId, this); // will add reviews of the same object to the map if hotel is valid
			return isReviewAdded;
		}
		finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Method to add local hotel reviews data to the main map
	 * @param hotelID - id of the hotel
	 * @param localData - local reviews data set for a hotel to be added to main map
	 */
	
	public void combine(String hotelID, ThreadSafeHotelData localData){
		try {
			lock.writeLock().lock();
			//writing local review set to the main map
			super.writeReviewsToMap(hotelID,localData.getReviewsSet());
		}
		finally {
			lock.writeLock().unlock();
		}
	}
	
	
	/**
	 * Overridden method to make it thread safe using read lock.
	 * Gets the list of all hotel ids sorted alphabetically.
	 * @return List of hotel ids
	 */
	@Override
	public List<String> getHotels() {
		try{
			lock.readLock().lock();
			return super.getHotels();
		}
		finally {
			lock.readLock().unlock();
		}
	}
	
	/**
	 * Adds attraction to attractionsMap. Overridden method to make it thread safe using write lock.
	 * @param id Tourist attraction id
	 * @param name Attraction name
	 * @param rating Rating for that attraction
	 * @param address Address of the attraction
	 */
	@Override
	public void addAttraction(String id, String name, double rating, String address) {
		try {
			lock.writeLock().lock();
			super.addAttraction(id, name, rating, address);
		} finally{
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Writes attractions data from local list to main map. Using write lock to make it thread safe
	 * @param hotelID
	 * @param localData
	 */
	public void combineAllAttractions(String hotelID, ThreadSafeHotelData localData) {
		try {
			lock.writeLock().lock();
			super.writeAttractionsToMap(hotelID, (ArrayList<TouristAttraction>) localData.getTouristAttractionsList());
			
		} finally{
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Overridden method to make it thread safe using write lock. Adds descriptions for a hotel.
	 * @param hotelID hotel id
	 * @param hotelDetails hotel details with area and property description
	 */
	@Override
	public void addDescription(String hotelID, HotelDetails hotelDetails) {
		try {
			lock.writeLock().lock();
			super.addDescription(hotelID, hotelDetails);
		} finally{
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Overridden method to make it thread safe using write lock. Calls the parent printDescriptions method
	 * @param filename output file
	 */
	@Override
	public void printDescriptions(Path filename) {
		try {
			lock.readLock().lock();
			super.printDescriptions(filename);
		} finally{
			lock.readLock().unlock();
		}
		
	}
	
	/**
	 * Overridden method to make it thread safe using write lock. Prints attraction for a hotel.
	 * @param filename name of the file to print to
	 */
	@Override
	public void printAttractions(Path filename) {
		try {
			lock.readLock().lock();
			super.printAttractions(filename);
		} finally{
			lock.readLock().unlock();
		}
		
	}
}


