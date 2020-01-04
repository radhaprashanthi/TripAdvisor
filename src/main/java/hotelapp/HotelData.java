package hotelapp;

import jettyServer.HotelDatabaseHandler;
import jettyServer.ReviewDatabaseHandler;
import jettyServer.Status;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.*;

/**
 * Used to store hotel data, attractions data and hotel descriptions.
 */
public class HotelData {
	
	private Map<String, HotelDetails> hotelsMap;
	private Map<String, TreeSet<HotelReview>> reviewsMap;
	private TreeSet<HotelReview> reviewsSet; //stores all the reviews to a local set
	
	private Map<String, List<TouristAttraction>> touristAttractionsMap;
	private List<TouristAttraction> touristAttractionsList;
	private Map<String, HotelDetails> descriptionsMap;
	
	protected static final HotelDatabaseHandler hotelHandler = HotelDatabaseHandler.getInstance();
	
	protected static final ReviewDatabaseHandler reviewHandler = ReviewDatabaseHandler.getInstance();
	
	/**
	 * Constructor of this class. Initializes all instance variables.
	 */
	public HotelData() {
		this.hotelsMap = new HashMap<>();
		this.reviewsMap = new HashMap<>();
		/**sorts reviews based on review date, username and review ID*/
		reviewsSet = new TreeSet<>((hr1, hr2) -> {
			
			if (hr1.getReviewDate().after(hr2.getReviewDate())) {
				return -1;
			} else if (hr1.getReviewDate().equals(hr2.getReviewDate())) {
				if (hr1.getUserNickname().compareTo(hr2.getUserNickname()) == 0) {
					return hr1.getReviewId().compareTo(hr2.getUserNickname());
				} else {
					return hr1.getUserNickname().compareTo(hr2.getUserNickname());
				}
			}
			return 1;
			
		}
		);
		
		this.touristAttractionsMap = new HashMap<>();
		touristAttractionsList = new ArrayList<>();
		descriptionsMap = new HashMap<>();
	}
	
	/**
	 * Adds hotel information to hotelsMap
	 * @param hotelId - id of the hotel
	 * @param hotelName - name of the hotel
	 * @param city - city where the hotel is located
	 * @param state - state where the hotel is
	 * @param streetAddress - street address of the hotel
	 * @param lat - latitude information about hotel
	 * @param lon - longitude information about hotel
	 */
	public void addHotel(String hotelId, String hotelName, String city, String state, String streetAddress, double lat,
	                     double lon) {
		HotelDetails hotelDetails = new HotelDetails(hotelId, hotelName, city, state, streetAddress, lat, lon);
		hotelsMap.put(hotelId, hotelDetails);
	}
	
	/**
	 * Adds hotel information to DB
	 * @param hotelId - id of the hotel
	 * @param hotelName - name of the hotel
	 * @param city - city where the hotel is located
	 * @param state - state where the hotel is
	 * @param streetAddress - street address of the hotel
	 * @param lat - latitude information about hotel
	 * @param lon - longitude information about hotel
	 */
	public void addHotelToDB(String hotelId, String hotelName, String city, String state, String streetAddress, double lat,
	                         double lon){
		Status status = hotelHandler.checkHotelExists(hotelId);
		if (status == Status.OK) {
			Status s = hotelHandler.addHotel(hotelId, hotelName, streetAddress, city, state, lat, lon);
		}
	}
	
	/**
	 * Used to load hotels data to DB from Map.
	 */
	public void addHotelsMapToDB() {
		for (HotelDetails hotel: hotelsMap.values()) {
			String hotelName = hotel.getName();
			String hotelId = hotel.getId();
			double lat = hotel.getLatitude();
			double lon = hotel.getLongitude();
			String streetAddress = hotel.getStreet();
			String city = hotel.getCity();
			String state = hotel.getState();
			addHotelToDB(hotelId, hotelName, streetAddress, city, state, lat, lon);
		}
	}
	
	/**
	 * Getter method to return set of reviews for a hotel
	 * @return set of hotel reviews
	 */
	public TreeSet<HotelReview> getReviewsSet() {
		return reviewsSet;
	}
	
	
	/**
	 * Validates review data and throws exception if rating or date format is invalid.
	 * Adds the review to local tree set.
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
	 * @return true if successful, false if unsuccessful because of invalid date
	 *         or rating. Needs to catch and handle the following exceptions:
	 *         ParseException if the date is invalid InvalidRatingException if
	 *         the rating is out of range
	 */
	public boolean addReview(String hotelId, String reviewId, int rating, String reviewTitle, String review,
	                         boolean isRecom, String date, String username) {
		
		try {
			if(rating > 5 || rating < 0){
				throw new InvalidParameterException("Invalid rating "+rating);
			}
			HotelReview hotelReview = new HotelReview(reviewId, hotelId, username, rating, isRecom, reviewTitle, review, date);
			reviewsSet.add(hotelReview);
			addReviewToDB(hotelId, reviewId, rating, reviewTitle, review, isRecom, date, username);
			return true;
			
		} catch (Exception e){
			System.err.println(e);
		}
		return false;
	}
	
	/**
	 * Adds the review to DB.
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
	 */
	public void addReviewToDB(String hotelId, String reviewId, int rating, String reviewTitle, String review,
	                          boolean isRecom, String date, String username) {
		Status status = reviewHandler.checkReviewExists(reviewId);
		if (status == Status.OK) {
			Status s = reviewHandler.addReview(reviewId, hotelId, username, rating, isRecom, reviewTitle, review, date);
		}
	}
	
	/**
	 * Used to load reviews to DB from Map
	 */
	public void addReviewsMapToDB() {
		for (String hotelId: reviewsMap.keySet()) {
			for (HotelReview review: reviewsMap.get(hotelId)) {
				String hotelID = review.getHotelId() ;
				String reviewId = review.getReviewId();
				String reviewTitle = review.getTitle();
				String reviewText = review.getReviewText();
				String username = review.getUserNickname();
				int rating = review.getRating();
				String date = review.getReviewDate().toString();
				boolean isRecom = review.isRecommended();
				
				addReviewToDB(hotelId, reviewId, rating, reviewTitle, reviewText, isRecom, date, username);
			}
		}
	}
	
	/**
	 * Return an alphabetized list of the ids of all hotels
	 *
	 * @return - list of strings with hotel id
	 */
	public List<String> getHotels() {
		List<String> hotelIDsList = new ArrayList<>();
		if(!hotelsMap.isEmpty()) {
			for (String hotelID : hotelsMap.keySet()) {
				hotelIDsList.add(hotelID);
			}
			Collections.sort(hotelIDsList); //sorts hotel ids alphabetically
		}
		return hotelIDsList;
	}
	
	/**
	 * Method to add local reviews set to the main reviewsMap
	 * Add review only if it is a valid hotel
	 * @param hotelID - id of the hotel
	 * @param localReviewsSet - set of reviews for the passed hotelID
	 */
	public void writeReviewsToMap(String hotelID, TreeSet<HotelReview> localReviewsSet){
		if(hotelsMap.keySet().contains(hotelID)) {
			reviewsMap.put(hotelID, localReviewsSet);
		}
	}
	
	
	/**
	 * Adds tourist attractions near by to a hotel to a set
	 * @param id Tourist attraction id
	 * @param name Attraction name
	 * @param rating Rating for that attraction
	 * @param address Address of the attraction
	 */
	public void addAttraction(String id, String name, double rating, String address) {
		TouristAttraction touristAttraction = new TouristAttraction(id, name, rating, address);
		touristAttractionsList.add(touristAttraction);
	}
	
	/**
	 * Writes all attractions for a hotel to a map
	 * @param hotelID Id of a hotel
	 * @param touristAttractionsList List of attractions near by the hotel id.
	 */
	public void writeAttractionsToMap(String hotelID, ArrayList<TouristAttraction> touristAttractionsList) {
		touristAttractionsMap.put(hotelID, touristAttractionsList);
	}
	
	/**
	 * Used while copying to main thread from local thread
	 * @return list of tourist attractions
	 */
	public List<TouristAttraction> getTouristAttractionsList() {
		return touristAttractionsList;
	}
	
	/**
	 * Returns the attractions near by a hotel stored in a map
	 * @param hotelID Id of a hotel
	 * @return List of attractions by hotel id.
	 */
	public List<TouristAttraction> findAttractionsByHotelID(String hotelID){
		if (touristAttractionsMap.get(hotelID) != null) {
			List<TouristAttraction> attractions = new ArrayList<>();
			for (TouristAttraction attraction:touristAttractionsMap.get(hotelID)) {
				attractions.add(attraction);
			}
			return attractions;
		}
		return null;
	}
	
	/**
	 * Add description to a map
	 * @param hotelID hotel id
	 * @param hotelDetails hotel details with area and property description
	 */
	public void addDescription(String hotelID, HotelDetails hotelDetails) {
		descriptionsMap.put(hotelID, hotelDetails);
	}
	
	/**
	 * Used in HotelSearch class. Gets the descriptions for a hotel by id. Cloning the object for encapsulation.
	 * @param id hotel id
	 * @return HotelDetails object with area and property descriptions
	 */
	public HotelDetails findDescriptionsByHotelId(String id) {
		HotelDetails hotelDetails = descriptionsMap.get(id);
		HotelDetails descriptionsClone = null;
		if (hotelDetails != null) {
			descriptionsClone = new HotelDetails(hotelDetails.getId(), hotelDetails.getName(), hotelDetails.getCity(), hotelDetails.getState(), hotelDetails.getStreet(), hotelDetails.getLatitude(), hotelDetails.getLongitude());
			descriptionsClone.setAreaDescription(hotelDetails.getAreaDescription());
			descriptionsClone.setPropertyDescription(hotelDetails.getPropertyDescription());
		}
		return descriptionsClone;
	}
	
	/**
	 * Get hotel details by id. Fetched from hotelsMap. Cloning the object to protect encapsulation
	 * @param id hotel id
	 * @return hotel details
	 */
	public HotelDetails findHotelById(String id) {
		if (id != null) {
			HotelDetails hotelDetails = hotelsMap.get(id);
			if (hotelDetails != null) {
				HotelDetails hotelDetailsClone = new HotelDetails(hotelDetails.getId(), hotelDetails.getName(), hotelDetails.getCity(), hotelDetails.getState(), hotelDetails.getStreet(), hotelDetails.getLatitude(), hotelDetails.getLongitude());
				return hotelDetailsClone;
			}
		}
		return null;
	}
	
	/** Get list of hotel reviews by hotel id.
	 * @param hotelId Takes integer value
	 * @return unmodifiable reference to the list of hotel reviews
	 */
	public ArrayList<HotelReview> findReviewsByHotelId(String hotelId, int count){
		if (hotelId != null && count > 0) {
			TreeSet<HotelReview> list = reviewsMap.get(hotelId);
			if (list != null) {
				ArrayList<HotelReview> reviewsList = new ArrayList<>();
				int counter = 0;
				for (HotelReview review : list) {
					if (counter < count) {
						reviewsList.add(review);
						counter++;
					}
				}
				return reviewsList;
			}
		}
		return null;
	}
	
	
	/** Print attractions near the hotels to a file.
	 * The format is described in the project description.
	 *
	 * @param filename name of the file to print to
	 */
	public void printAttractions(Path filename) {
		// FILL IN CODE
		try {
			File file = new File (filename.toString());
			PrintWriter printWriter = new PrintWriter (file);
			for (String hotelID: getHotels()) {
				printWriter.write("Attractions near ");
				HotelDetails hotelDetails = findHotelById(hotelID);
				printWriter.write(hotelDetails.getId());
				printWriter.write(", ");
				printWriter.write(hotelDetails.getName());
				printWriter.write(System.lineSeparator());
				if (findAttractionsByHotelID(hotelID) != null) {
					for (TouristAttraction attraction : findAttractionsByHotelID(hotelID)) {
						printWriter.write(attraction.toString());
						printWriter.write(System.lineSeparator());
					}
				}
				printWriter.write(System.lineSeparator());
				printWriter.write("++++++++++++++++++++");
				printWriter.write(System.lineSeparator());
			}
			
			printWriter.flush();
			printWriter.close();
			
		} catch (IOException e) {
			System.out.println(e);;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Prints property descriptions and area descriptions for each hotel from
	 * the ThreadSafeHotelData to the given file. Format specified in the project description.
	 * @param filename output file
	 */
	public void printDescriptions(Path filename) {
		// FILL IN CODE
		try {
			File file = new File (filename.toString());
			PrintWriter printWriter = new PrintWriter (file);
			for (String hotelID: getHotels()) {
				if(findDescriptionsByHotelId(hotelID) != null) {
					printWriter.write(hotelID);
					printWriter.write(System.lineSeparator()+System.lineSeparator());
					
					HotelDetails hotelDetails = findDescriptionsByHotelId(hotelID);
					if (hotelDetails.getAreaDescription() != null) {
						printWriter.write(hotelDetails.getAreaDescription());
						printWriter.write(System.lineSeparator()+System.lineSeparator());
					}
					
					if (hotelDetails.getPropertyDescription() != null) {
						printWriter.write(hotelDetails.getPropertyDescription());
						printWriter.write(System.lineSeparator());
					}
					printWriter.write("++++++++++++++++++++");
					printWriter.write(System.lineSeparator());
				}
			}
			printWriter.flush();
			printWriter.close();
			
		} catch (IOException e) {
			System.out.println(e);;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}


