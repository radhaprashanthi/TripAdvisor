package jettyServer;

import hotelapp.HotelDetails;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles hotel database table creating, updation and deletion
 * Also performs other queries like selecting distinct cities,
 * search hotels based on city and hotel name.
 */
public class HotelDatabaseHandler {
	
	/** Makes sure only one database handler is instantiated. */
	private static HotelDatabaseHandler singleton = new HotelDatabaseHandler();
	
	/** Used to determine if necessary tables are provided. */
	private static final String TABLES_SQL =
			"SHOW TABLES LIKE 'hotel_details';";
	
	/** Used to create hotel_details table. */
	private static final String CREATE_SQL =
			"CREATE TABLE hotel_details (" +
			"id VARCHAR(10) NOT NULL PRIMARY KEY, " +
			"name VARCHAR(256) NOT NULL, " +
			"street VARCHAR(512), " +
			"city VARCHAR(32), " +
			"state VARCHAR(32), " +
			"latitude DOUBLE(8,2), " +
			"longitude DOUBLE(8,2), " +
			"areadesc VARCHAR(4000), " +
			"propertydesc VARCHAR(4000));";
	
	/** Used to insert a new hotel into the database. */
	private static final String ADDHOTEL_SQL =
			"INSERT INTO hotel_details (id, name, street, city, state, latitude, longitude) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?);";
	
	/** Used to determine if a hotel id already exists. */
	private static final String CHECKHOTEL_SQL =
			"SELECT id FROM hotel_details WHERE id = ?";
	
	/** Used to remove a hotel from the database. */
	private static final String DELETE_SQL =
			"DELETE FROM hotel_details WHERE id = ?";
	
	/** Used to get a distinct city for hotels from the database. */
	private static final String CITIES_SQL =
			"SELECT DISTINCT city FROM hotel_details";
	
	/** Used to search hotels by city from the database. */
	private static final String HOTELS_BY_CITY_SQL =
			"SELECT id, name, street, city, state FROM hotel_details WHERE city = ?";
	
	/** Used to search hotels by city and name from the database. */
	private static final String HOTELS_BY_CITY_NAME_SQL =
			"SELECT id, name, street, city, state FROM hotel_details WHERE name LIKE ? AND city = ?";
	
	/** Used to search hotels by name from the database. */
	private static final String HOTELS_BY_NAME_SQL =
			"SELECT id, name, street, city, state FROM hotel_details WHERE name LIKE ?";
	
	/** Used to search hotels by name from the database. */
	private static final String ALLHOTELS_SQL =
			"SELECT id, name, street, city, state, latitude, longitude FROM hotel_details";
	
	/** Used to get hotel by id from the database. */
	private static final String HOTEL_BY_ID_SQL =
			"SELECT id, name, street, city, state, latitude, longitude FROM hotel_details WHERE id = ?";
			
	
	/** Used to configure connection to database. */
	private DatabaseConnector db;
	
	/**
	 * Initializes a database handler for the hotels. Private constructor
	 * forces all other classes to use singleton.
	 */
	private HotelDatabaseHandler() {
		Status status = Status.OK;
		
		try {
			db = new DatabaseConnector("database.properties");
			status = db.testConnection() ? setupTables() : Status.CONNECTION_FAILED;
		}
		catch (FileNotFoundException e) {
			status = Status.MISSING_CONFIG;
		}
		catch (IOException e) {
			status = Status.MISSING_VALUES;
		}
		
		if (status != Status.OK) {
			System.err.println(status.message());
		}
	}
	
	/**
	 * Gets the single instance of the database handler.
	 *
	 * @return instance of the database handler
	 */
	public static HotelDatabaseHandler getInstance() {
		return singleton;
	}
	
	/**
	 * Checks to see if a String is null or empty.
	 * @param text - String to check
	 * @return true if non-null and non-empty
	 */
	public static boolean isBlank(String text) {
		return (text == null) || text.trim().isEmpty();
	}
	
	/**
	 * Checks if necessary table exists in database, and if not tries to
	 * create it.
	 *
	 */
	private Status setupTables() {
		Status status = Status.ERROR;
		
		try (
				Connection connection = db.getConnection();
				Statement statement = connection.createStatement();
		) {
			if (!statement.executeQuery(TABLES_SQL).next()) {
				// Table missing, must create
				System.out.println("Creating tables...");
				statement.executeUpdate(CREATE_SQL);
				
				// Check if create was successful
				if (!statement.executeQuery(TABLES_SQL).next()) {
					status = Status.CREATE_FAILED;
				}
				else {
					status = Status.OK;
				}
			}
			else {
				System.out.println("Tables found.");
				status = Status.OK;
			}
		}
		catch (Exception ex) {
			status = Status.CREATE_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Tests if a hotel already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection - active database connection
	 * @param id - hotel id to check
	 * @return Status.OK if user does not exist in database
	 * @throws SQLException
	 */
	private Status checkHotelExists(Connection connection, String id) {
		
		assert connection != null;
		assert id != null;
		
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(CHECKHOTEL_SQL);
		) {
			statement.setString(1, id);
			
			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.DUPLICATE_HOTEL : Status.OK;
		}
		catch (SQLException e) {
			System.err.println(e.getMessage() + " " + e);
			status = Status.SQL_EXCEPTION;
		}
		
		return status;
	}
	
	/**
	 * Tests if a hotel already exists in the database.
	 *
	 * @see #checkHotelExists(Connection, String)
	 * @param id - hotel id to check
	 * @return Status.OK if user does not exist in database
	 */
	public Status checkHotelExists(String id) {
		Status status = Status.ERROR;
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkHotelExists(connection, id);
		}
		catch (SQLException e) {
			status = Status.CONNECTION_FAILED;
			System.err.println(e.getMessage() + " " + e);
		}
		
		return status;
	}
	
	/**
	 * Adds hotel to database
	 * @param id - hotel id
	 * @param name - hotel name
	 * @param street - hotel address
	 * @param city - hotel city
	 * @param state - hotel state
	 * @param latitude - latitude geo co-ordinate of hotel
	 * @param longitude - longitude geo co-ordinate of hotel
	 * @return
	 */
	private Status addHotel(Connection connection, String id, String name, String street, String city, String state, double latitude, double longitude) {
		
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(ADDHOTEL_SQL);
		) {
			statement.setString(1, id);
			statement.setString(2, name);
			statement.setString(3, street);
			statement.setString(4, city);
			statement.setString(5, state);
			statement.setDouble(6, latitude);
			statement.setDouble(7, longitude);
			statement.executeUpdate();
			
			status = Status.OK;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			System.err.println(ex.getMessage() + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Adds hotel to database
	 * @param id - hotel id
	 * @param name - hotel name
	 * @param street - hotel address
	 * @param city - hotel city
	 * @param state - hotel state
	 * @param latitude - latitude geo co-ordinate of hotel
	 * @param longitude - longitude geo co-ordinate of hotel
	 * @return
	 */
	public Status addHotel(String id, String name, String street, String city, String state, double latitude, double longitude) {
		Status status = Status.ERROR;
		System.err.println("Adding " + id + ".");
		
		// make sure we have non-null and non-emtpy values for hotel
		if (isBlank(id) || isBlank(name)) {
			status = Status.INVALID_HOTEL;
			System.err.println(status);
			return status;
		}
		
		// try to connect to database and test for duplicate user
		System.out.println(db);
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkHotelExists(connection, id);
			
			// if okay so far, try to insert new user
			if (status == Status.OK) {
				status = addHotel(connection, id, name, street, city, state, latitude, longitude);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Removes a hotel from the database if the hotel id is
	 * provided correctly.
	 * @param connection - database connection
	 * @param id - hotel id to remove
	 * @return status.OK if removal successful
	 */
	private Status removeHotel(Connection connection, String id) {
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(DELETE_SQL);
		) {
			statement.setString(1, id);
			
			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_HOTEL;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Removes a hotel from the database if hotel id is
	 * provided correctly.
	 *
	 * @param id - hotel id to remove
	 * @return Status.OK if removal successful
	 */
	public Status removeHotel(String id) {
		Status status = Status.ERROR;
		
		System.out.println("Removing hotel " + id + ".");
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkHotelExists(connection, id);
			
			if(status == Status.OK) {
				status = removeHotel(connection, id);
			}
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Used to get all the distinct cities for hotels
	 * @return Results of cities from DB
	 */
	private ResultSet getAllCities(Connection connection) {
		Status status = Status.ERROR;
		ResultSet results = null;
		try {
			PreparedStatement statement = connection.prepareStatement(CITIES_SQL);
			results = statement.executeQuery();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	/**
	 * Get all the cities to use for search
	 * @return list of distinct cities in database
	 */
	public List getCities() {
		try (Connection connection = db.getConnection();) {
			ResultSet results = getAllCities(connection);
			if (results != null) {
				List<String> cities = new ArrayList<>();
				while (results.next()) {
					cities.add(results.getString(1));
				}
				return cities;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Search all the hotels by name and city
	 * @param connection - database connection
	 * @param name - hotel name
	 * @param city - city of hotel
	 * @return Results of hotels from DB
	 */
	private ResultSet searchHotels(Connection connection, String name, String city) {
		Status status = Status.ERROR;
		ResultSet results = null;
		try{
			PreparedStatement statement = null;
			city = city.replaceAll("\"", "");
			name = "%" + name +"%";
			
			if (!isBlank(name) && !isBlank(city)) {
				statement = connection.prepareStatement(HOTELS_BY_CITY_NAME_SQL);
				statement.setString(1, name);
				statement.setString(2, city);
			}
			else if (!isBlank(name) && isBlank(city)) {
				statement = connection.prepareStatement(HOTELS_BY_NAME_SQL);
				statement.setString(1, name);
				
			}
			else if (isBlank(name) && !isBlank(city)) {
				statement = connection.prepareStatement(HOTELS_BY_CITY_SQL);
				statement.setString(1, city);
			}
			else {
				//statement = connection.prepareStatement(ALLHOTELS_SQL);
			}
			
			if (statement != null)
				results = statement.executeQuery();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	/**
	 * Search all the hotels by name and city
	 * @param name - hotel name
	 * @param city - city of hotel
	 * @return Results of hotels from DB
	 */
	public List searchHotels(String name, String city) {
		try (Connection connection = db.getConnection();){
			DecimalFormat df = new DecimalFormat("#.#");
			ReviewBaseServlet reviewBaseServlet = new ReviewBaseServlet();
			ResultSet results = searchHotels(connection, name, city);
			if (results != null) {
				List<HotelDetails> hotels = new ArrayList<>();
				while (results.next()) {
					HotelDetails hotel = new HotelDetails(results.getString(1), results.getString(2), results.getString(3), results.getString(4), results.getString(5));
					hotel.setAvgRating(df.format(reviewBaseServlet.getAvgRating(hotel.getId())));
					hotels.add(hotel);
				}
				return hotels;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Used to get hotel by id
	 * @param connection - database connection
	 * @param id - hotel id
	 * @return hotel by id from DB
	 */
	private ResultSet getHotelById(Connection connection, String id) {
		Status status = Status.ERROR;
		ResultSet result = null;
		try {
			PreparedStatement statement = connection.prepareStatement(HOTEL_BY_ID_SQL);
			statement.setString(1, id);
			result = statement.executeQuery();
			
			//status = results.next()? Status.OK: Status.INVALID_HOTEL;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Used to get hotel by id
	 * @param id - hotel id
	 * @return hotel by id from DB
	 */
	public HotelDetails getHotel(String id) {
		try (Connection connection = db.getConnection();) {
			ResultSet results = getHotelById(connection, id);
			HotelDetails hotel = null;
			if (results != null) {
				if (results.next()) {
					hotel = new HotelDetails(results.getString(1), results.getString(2), results.getString(3), results.getString(4), results.getString(5), results.getDouble(6), results.getDouble(7));
				}
				return hotel;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Get all the hotels
	 * @param connection - database connection
	 * @return Results of hotels from DB
	 */
	private ResultSet getAllHotels(Connection connection) {
		Status status = Status.ERROR;
		ResultSet results = null;
		try{
			PreparedStatement statement = connection.prepareStatement(ALLHOTELS_SQL);
			results = statement.executeQuery();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	/**
	 * Fetch all the hotels from DB
	 * @return Results of hotels from DB
	 */
	public List getAllHotels() {
		try (Connection connection = db.getConnection();) {
			ReviewBaseServlet reviewBaseServlet = new ReviewBaseServlet();
			ResultSet results = getAllHotels(connection);
			if (results != null) {
				List<HotelDetails> hotels = new ArrayList<>();
				while (results.next()) {
					HotelDetails hotel = new HotelDetails(results.getString(2), results.getDouble(6), results.getDouble(7));
					hotels.add(hotel);
				}
				return hotels;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
