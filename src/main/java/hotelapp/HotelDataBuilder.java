package hotelapp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class HotelDataBuilder. Loads hotel info from input files to ThreadSafeHotelData (using multithreading). */
public class HotelDataBuilder {
	private ThreadSafeHotelData hdata; // the "big" ThreadSafeHotelData that will contain all hotel and reviews info
	private ExecutorService exec;
	
	/** Constructor for class HotelDataBuilder.
	 *  @param data */
	public HotelDataBuilder(ThreadSafeHotelData data) {
		// FILL IN CODE
		this.hdata = data;
		exec = Executors.newFixedThreadPool(20);
	}
	
	/** Constructor for class HotelDataBuilder that takes ThreadSafeHotelData and
	 * the number of threads to create as a parameter.
	 * @param data
	 * @param numThreads
	 */
	
	/**
	 * Read the json file with information about the hotels and load it into the
	 * appropriate data structure(s).
	 * @param jsonFilename
	 */
	public void loadHotelInfo(String jsonFilename) {
		// FILL IN CODE (from lab 1)
		try{
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObject = (JsonObject) jsonParser.parse(new FileReader(jsonFilename));
			JsonArray hotels = jsonObject.getAsJsonArray("sr");
			
			String hotelName = "";
			String hotelID = "";
			double lat = 0.0;
			double lon = 0.0;
			String street = "";
			String city = "";
			String state = "";
			for(int i=0; i < hotels.size(); i++){
				JsonObject hotelObject = hotels.get(i).getAsJsonObject();
				hotelName = hotelObject.get("f").getAsString();
				hotelID = hotelObject.get("id").getAsString();
				JsonObject location = hotelObject.getAsJsonObject("ll");
				lat = location.get("lat").getAsDouble();
				lon = location.get("lng").getAsDouble();
				street = hotelObject.get("ad").getAsString();
				city = hotelObject.get("ci").getAsString();
				state = hotelObject.get("pr").getAsString();
				
				hdata.addHotel(hotelID, hotelName, city, state, street, lat, lon);
				
			}
		} catch (FileNotFoundException e) {
			System.out.println(e);
			System.exit(0);
		}
	}
	
	/** Loads reviews from json files. Recursively processes subfolders.
	 *  Each json file with reviews should be processed concurrently (you need to create a new runnable job for each
	 *  json file that you encounter)
	 *  @param dir
	 */
	public void loadReviews(Path dir) {
		parseReviews(dir);
		exec.shutdown();
		try {
			exec.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			System.out.println(e);
			//System.out.printlnerror(e);
		}
	}
	
	/** Prints all hotel info to the file. Calls hdata's printToFile method. */
	/*public void printToFile(Path filename) {
		//hdata.printToFile(filename);
	}*/
	
	/**
	 * Used to parse reviews and store it in data structures
	 * @param dir path to the reviews directory
	 */
	public void parseReviews(Path dir) {
		try (DirectoryStream<Path> filesList = Files.newDirectoryStream(dir)) {
			for (Path file : filesList) {
				// recursive call to read the name of each file in the directory
				if (Files.isDirectory(file)) {
					parseReviews(file);
				} else {
					exec.submit(new Worker(file));
					//System.out.println("Executing thread" + exec);
				}
			}
		} catch (IOException e) {
			System.out.println("Can not open directory: " + dir);
		}
		catch (Exception e) {
			System.out.println("Exception while running the reviews worker: " + e);
		}
	}
	// FILL IN CODE: add an inner class and other methods as needed
	// Note: You need to have an inner class that implements Runnable and parses each json file with reviews
	
	/**
	 * Inner class to create Worker threads to parse review file and write to map
	 */
	public class Worker implements Runnable {
		
		private Path filePath;
		private ThreadSafeHotelData localData;
		
		/**Constructor of this class*/
		public Worker(Path filePath) {
			this.filePath = filePath;
			//Create local instance of ThreadSafeHotelData to add all reviews to local set
			localData = new ThreadSafeHotelData();
		}
		
		/**
		 * Parses all the reviews of a hotel
		 */
		@Override
		public void run() {
			try {
				JsonParser jsonParser = new JsonParser();
				JsonObject jsonObject = (JsonObject) jsonParser.parse(new FileReader(filePath.toString()));
				JsonObject reviewCollection = jsonObject.getAsJsonObject("reviewDetails").getAsJsonObject(
						"reviewCollection");
				JsonArray reviewList = reviewCollection.getAsJsonArray("review");
				
				String hotelId = "";
				String reviewId, reviewTitle, review, username;
				int rating;
				String date;
				boolean isRecom;
				
				for (int i = 0; i < reviewList.size(); i++) {
					JsonObject reviewObject = reviewList.get(i).getAsJsonObject();
					hotelId = reviewObject.get("hotelId").getAsString();
					reviewId = reviewObject.get("reviewId").getAsString();
					rating = reviewObject.get("ratingOverall").getAsInt();
					reviewTitle = reviewObject.get("title").getAsString();
					review = reviewObject.get("reviewText").getAsString();
					username = reviewObject.get("userNickname").getAsString();
					if (username.isBlank()) username = "Anonymous";
					date = reviewObject.get("reviewSubmissionTime").getAsString();
					isRecom = reviewObject.get("isRecommended").getAsBoolean();
					
					//adding reviews to local ThreadSafeHotelData
					localData.addReview(hotelId, reviewId, rating, reviewTitle, review, isRecom, date, username);
				}
				//adding localData instance of reviews set to the main thread map of reviews
				hdata.combine(hotelId, localData);
				
			} catch (IOException e) {
				System.out.println("Could not read the file: " + e);
				//System.out.printlnerror("Could not read the file: " + e);
			}
			
		}
	}
}
