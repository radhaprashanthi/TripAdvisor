package hotelapp;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


/** The main class for project 1.
 * The main function should take the following 4 command line arguments:
 * -hotels hotelFile -radius radiusInMiles
 *
 * and read general information about the hotels from the hotelFile (a JSON file)
 * and read fetch tourist attractions nearby hotel for the radius passed.
 * TParse descriptions for the hotels.
 * See Readme for details.
 */
public class HotelSearch {
	/**
	 * Search for hotel details, tourist attractions and descriptions by hotel id
	 * @param query what you want to find
	 * @param threadSafeHotelData object of ThreadsafeHotelData
	 */
	private void searchQuery(String query, ThreadSafeHotelData threadSafeHotelData) {
		
		String[] queryParam = query.split(" "); //split by space
		
		switch (queryParam[0].toLowerCase()) {
			
			case "find": HotelDetails hotelDetails = threadSafeHotelData.findHotelById(queryParam[1]);
				if (hotelDetails == null)
					System.out.println("Hotel doesn't exist with this id. Please provide the right hotel id!");
				else
					System.out.println(hotelDetails);
				break;
			case "findreviews": List<HotelReview> hotelReviews = threadSafeHotelData.findReviewsByHotelId(queryParam[1], 20);
				if (hotelReviews == null) {
					if(threadSafeHotelData.findHotelById(queryParam[1])!= null)
						System.out.println("There are no reviews for this hotel id.");
					else
						System.out.println("Hotel doesn't exist with this id. Please provide right id.");
				}
				else
					System.out.println(hotelReviews);
				break;
			
			case "findattractions": TouristAttractionFinder attractionFinder = new TouristAttractionFinder(threadSafeHotelData);
				attractionFinder.fetchAttractions(queryParam[1],2);
				List<TouristAttraction> touristAttractions = threadSafeHotelData.findAttractionsByHotelID(queryParam[1]);
				if (touristAttractions == null) {
					System.out.println("No tourist attractions found near this hotel id!");
				} else {
					String hotelName = threadSafeHotelData.findHotelById(queryParam[1]).getName();
					System.out.println("Attractions near " + queryParam[1] + ", " + hotelName);
					for (TouristAttraction attraction: touristAttractions)
						System.out.println(attraction);
					System.out.println("++++++++++++++++++++");
				}
				break;
				
			case "finddescriptions": TouristAttractionFinder descriptions = new TouristAttractionFinder(threadSafeHotelData);
				descriptions.parseHTML(queryParam[1]);
				HotelDetails hotelDescription = threadSafeHotelData.findDescriptionsByHotelId(queryParam[1]);
				if (hotelDescription != null) {
					System.out.println(hotelDescription.getId());
					System.out.println();
					System.out.println(hotelDescription.getAreaDescription());
					System.out.println();
					System.out.println(hotelDescription.getPropertyDescription());
				} else {
					System.out.println("Hotel descriptions doesn't exist for this id!");
				}
				break;
				
			default: System.out.println("Please enter the right query!");
		}
	}
	
	/**
	 * Driver method to preload the maps and search for hotels, attractions and descriptions
	 * @param args command line arguments
	 */
	public static ThreadSafeHotelData loadHotelData (String[] args) {
		HashMap<String, String> argsMap = new HashMap<String, String>();
		
		if (args.length < 4) {
			System.out.println("Enter hotels json file path and reviews directory path");
			System.exit(0);
		}
		
		for (int i = 0; i < args.length; i += 2) {
			if (args[i].startsWith("-")) {
				argsMap.put(args[i], args[i + 1]);
			} else {
				System.out.println("Flag name should start with -");
				System.exit(0);
			}
		}
		
		ThreadSafeHotelData threadSafeHotelData = new ThreadSafeHotelData();
		HotelDataBuilder hdBuilder = new HotelDataBuilder(threadSafeHotelData);
		if (argsMap.get("-hotels") != null && argsMap.get("-hotels").endsWith(".json")) {
			hdBuilder.loadHotelInfo(argsMap.get("-hotels"));
		} else {
			System.out.println("Enter correct hotels json file path");
			System.exit(0);
		}
		
		if (argsMap.get("-reviews") != null) {
			hdBuilder.loadReviews(Paths.get(argsMap.get("-reviews")));
		} else {
			System.out.println("Enter correct reviews directory path");
		}
		return threadSafeHotelData;
	}
	
	
	public static void main(String[] args) {
		HotelSearch hotelSearch = new HotelSearch();
		ThreadSafeHotelData threadSafeHotelData = loadHotelData(args);
		
		
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter your search query");
		while (scan.hasNext()) {
			String query = scan.nextLine();
			if(query.equalsIgnoreCase("exit")){
				System.exit(0);
			}
			hotelSearch.searchQuery(query, threadSafeHotelData);
		}
	}
	
}
