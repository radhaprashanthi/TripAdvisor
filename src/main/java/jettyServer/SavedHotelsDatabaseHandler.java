package jettyServer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saved hotels DB table.
 * Performs creationg, updation, deletion of records from DB
 */
public class SavedHotelsDatabaseHandler {
	
	/** Makes sure only one database handler is instantiated. */
	private static SavedHotelsDatabaseHandler singleton = new SavedHotelsDatabaseHandler();
	
	/** Used to determine if necessary tables are provided. */
	private static final String TABLES_SQL =
			"SHOW TABLES LIKE 'saved_hotels';";
	
	/** Used to create saved_hotels table. */
	private static final String CREATE_SQL =
			"CREATE TABLE saved_hotels (id VARCHAR(64) NOT NULL, user VARCHAR(64) NOT NULL, PRIMARY KEY (id, user));";
	
	/** Used to insert a new hotel into the database. */
	private static final String SAVEHOTEL_SQL =
			"INSERT INTO saved_hotels (id, user) " +
			"VALUES (?, ?);";
	
	/** Used to determine if a hotel id is already saved. HotelDatabaseHandler*/
	private static final String CHECKHOTEL_SQL =
			"SELECT id FROM saved_hotels WHERE id = ? AND user = ?";
	
	
	/** Used to remove a hotel from the database. */
	private static final String DELETE_SQL =
			"DELETE FROM saved_hotels WHERE id = ? AND user = ?";
	
	/** Used to get all hotels saved by user from the database. */
	private static final String HOTELS_BY_USER_SQL =
			"SELECT id FROM saved_hotels WHERE user = ?";
	
	/** Used to delete all saved hotel by user from the database. */
	private static final String DELETE_ALLHOTELS_BY_USER_SQL =
			"DELETE FROM saved_hotels WHERE user = ?";
	
	/** Used to configure connection to database. */
	private DatabaseConnector db;
	
	/**
	 * Initializes a database handler for the hotels. Private constructor
	 * forces all other classes to use singleton.
	 */
	private SavedHotelsDatabaseHandler() {
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
	public static SavedHotelsDatabaseHandler getInstance() {
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
	 * @param user - user name
	 * @return Status.OK if hotel saved by the user does not exist in database
	 * @throws SQLException
	 */
	private Status checkSavedHotelExists(Connection connection, String id, String user) {
		
		assert connection != null;
		assert id != null;
		
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(CHECKHOTEL_SQL);
		) {
			statement.setString(1, id);
			statement.setString(2, user);
			
			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.DUPLICATE_SAVEHOTEL : Status.OK;
		}
		catch (SQLException e) {
			System.err.println(status + " " + e);
			status = Status.SQL_EXCEPTION;
		}
		
		return status;
	}
	
	/**
	 * Tests if a hotel already exists in the database.
	 *
	 * @see #checkSavedHotelExists(Connection, String, String)
	 * @param id - hotel id to check
	 * @param user - user name
	 * @return Status.OK if hotel saved by that user does not exist in database
	 */
	public Status checkSavedHotelExists(String id, String user) {
		Status status = Status.ERROR;
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkSavedHotelExists(connection, id, user);
		}
		catch (SQLException e) {
			status = Status.CONNECTION_FAILED;
			System.err.println(e.getMessage() + " " + e);
		}
		
		return status;
	}
	
	/**
	 * Adds a new hotel and user
	 * into the database if the hotel does not already exist
	 * with that user.
	 * @param connection - database connection
	 * @param id - id of new hotel
	 * @param user - name of the logged in user
	 * @return status ok if hotel addition is successful
	 */
	private Status saveHotel(Connection connection, String id, String user) {
		
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(SAVEHOTEL_SQL);
		) {
			statement.setString(1, id);
			statement.setString(2, user);
			statement.executeUpdate();
			
			status = Status.OK;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Adds a new hotel and user
	 * into the database if the hotel does not already exist
	 * with that user.
	 * @param id - id of new hotel
	 * @param user - name of logged in user
	 * @return status.ok if adding new hotel is successful
	 */
	public Status saveHotel(String id, String user) {
		Status status = Status.ERROR;
		System.out.println("Adding " + id + ".");
		
		// make sure we have non-null and non-emtpy values for hotel
		if (isBlank(id) || isBlank(user)) {
			status = Status.INVALID_SAVEHOTEL;
			System.err.println(status);
			return status;
		}
		
		// try to connect to database and test for duplicate user
		System.out.println(db);
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkSavedHotelExists(connection, id, user);
			
			// if okay so far, try to insert new user
			if (status == Status.OK) {
				status = saveHotel(connection, id, user);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Removes a hotel from the database if the hotel id and user are
	 * provided correctly.
	 * @param connection - database connection
	 * @param id - hotel id to remove
	 * @param user - user name
	 * @return status.OK if removal successful
	 */
	private Status removeSavedHotel(Connection connection, String id, String user) {
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(DELETE_SQL);
		) {
			statement.setString(1, id);
			statement.setString(2, user);
			
			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_SAVEHOTEL;
			System.out.println(statement);
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Removes a hotel from the database if hotel id and user are
	 * provided correctly.
	 *
	 * @param id - hotel id to remove
	 * @param user - user name
	 * @return Status.OK if removal successful
	 */
	public Status removeSavedHotel(String id, String user) {
		Status status = Status.ERROR;
		
		System.out.println("Removing saved hotel " + id + ".");
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkSavedHotelExists(connection, id, user);
			
			if(status != Status.OK) {
				status = removeSavedHotel(connection, id, user);
			}
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Used to get all the hotels saved by user
	 * @param connection - database connection
	 * @param user - user name
	 * @return Results of city from DB
	 */
	private ResultSet getSavedHotelsForUser(Connection connection, String user) {
		Status status = Status.ERROR;
		ResultSet results = null;
		try {
			PreparedStatement statement = connection.prepareStatement(HOTELS_BY_USER_SQL);
			statement.setString(1, user);
			results = statement.executeQuery();
			
		} catch (SQLException e) {
			System.err.println(e);
		}
		return results;
	}
	
	/**
	 * Get all the saved hotel ids for a user
	 * @param user - user name
	 * @return list of hotel ids in database
	 */
	public List getSavedHotelsForUser(String user) {
		try (Connection connection = db.getConnection();) {
			ResultSet results = getSavedHotelsForUser(connection, user);
			if (results != null) {
				List<String> hotelIds = new ArrayList<>();
				while (results.next()) {
					hotelIds.add(results.getString(1));
				}
				return hotelIds;
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		return null;
	}
	
	/**
	 * Removes all hotels from the database if the user is
	 * provided correctly.
	 * @param connection - database connection
	 * @param user - username to remove
	 * @return status.OK if removal successful
	 */
	private Status removeAllSavedHotelsByUser(Connection connection, String user) {
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(DELETE_ALLHOTELS_BY_USER_SQL);
		) {
			statement.setString(1, user);
			
			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_SAVEHOTEL;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Removes all hotels from the database if the user is
	 * provided correctly.
	 *
	 * @param user - username to remove
	 * @return status.OK if removal successful
	 */
	public Status removeAllSavedHotelsByUser(String user) {
		Status status = Status.ERROR;
		
		System.out.println("Removing saved hotel by " + user + ".");
		
		try (
				Connection connection = db.getConnection();
		) {
			status = removeAllSavedHotelsByUser(connection, user);
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
}
