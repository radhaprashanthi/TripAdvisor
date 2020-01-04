package jettyServer;

import hotelapp.HotelReview;
import hotelapp.HotelReviewsComparator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Set;
import java.util.TreeSet;

/**
 * Reviews database handler to handle all database table operations.
 * Create, update and delete reviews. Also performs average rating, reviews by hotel id.
 */
public class ReviewDatabaseHandler {
	
	/** Makes sure only one database handler is instantiated. */
	private static ReviewDatabaseHandler singleton = new ReviewDatabaseHandler();
	
	/** Used to determine if necessary tables are provided. */
	private static final String TABLES_SQL =
			"SHOW TABLES LIKE 'review_details';";
	
	/** Used to create review_details table. */
	private static final String CREATE_SQL =
			"CREATE TABLE review_details (" +
			"reviewid VARCHAR(64) PRIMARY KEY, " +
			"hotelid VARCHAR(32) NOT NULL, " +
			"user VARCHAR(512), " +
			"rating DOUBLE(8,2), " +
			"isrecommended BOOLEAN, " +
			"title VARCHAR(2000), " +
			"reviewtext VARCHAR(4000), " +
			"reviewdate VARCHAR(256));";
	
	
	/** Used to insert a new review into the database. */
	private static final String ADDREVIEW_SQL =
			"INSERT INTO review_details (reviewid, hotelid, user, rating, isrecommended, title, reviewtext, reviewdate) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	
	/** Used to determine if a review id already exists. */
	private static final String CHECKREVIEW_SQL =
			"SELECT reviewid FROM review_details WHERE reviewid = ?";
	
	
	/** Used to remove a review from the database. */
	private static final String DELETE_SQL =
			"DELETE FROM review_details WHERE reviewid = ?";
	
	/** Used to remove all reviews by user from the database. */
	private static final String DELETE_REVIEWS_BY_USER_SQL =
			"DELETE FROM review_details WHERE user = ?";
	
	/** Used to update review in the database. */
	private static final String UPDATEREVIEW_SQL =
			"UPDATE review_details SET title = ?, reviewtext = ?, rating = ?, isrecommended = ? WHERE reviewid = ?";
	
	/** Used to get reviews by hotel id from the database. */
	private static final String REVIEWS_BY_HOTELID_SQL =
			"SELECT reviewid, hotelid, user, rating, isrecommended, title, reviewtext, reviewdate FROM review_details WHERE hotelid = ?";
	
	/** Used to get review by review id from the database. */
	private static final String REVIEW_BY_REVIEWID_SQL =
			"SELECT reviewid, hotelid, user, rating, isrecommended, title, reviewtext, reviewdate FROM review_details WHERE reviewid = ?";
	
	/** Used to determine if a review id already exists. */
	private static final String AVGRATING_SQL =
			"SELECT AVG(rating) AS avgRating FROM review_details WHERE hotelid = ?";
	
	/** Used to get review by review id from the database. */
	private static final String REVIEWS_BY_USER_SQL =
			"SELECT reviewid, hotelid, user, rating, isrecommended, title, reviewtext, reviewdate FROM review_details WHERE user = ?";
	
	
	/** Used to configure connection to database. */
	private DatabaseConnector db;
	
	/**
	 * Initializes a database handler for the hotels. Private constructor
	 * forces all other classes to use singleton.
	 */
	private ReviewDatabaseHandler() {
		Status status = Status.OK;
		
		try {
			// TODO Change to "database.properties" or whatever your file is called
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
	 */
	public static ReviewDatabaseHandler getInstance() {
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
			System.out.println(ex);
			status = Status.CREATE_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Tests if a review already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection - active database connection
	 * @param id - review id to check
	 * @return Status.OK if user does not exist in database
	 * @throws SQLException
	 */
	private Status checkReviewExists(Connection connection, String id) {
		
		assert connection != null;
		assert id != null;
		
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(CHECKREVIEW_SQL);
		) {
			statement.setString(1, id);
			
			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.OK : Status.INVALID_REVIEW;
		}
		catch (SQLException e) {
			System.err.println(e.getMessage() + " " + e);
			status = Status.SQL_EXCEPTION;
		}
		
		return status;
	}
	
	/**
	 * Tests if a review already exists in the database.
	 *
	 * @see #checkReviewExists(Connection, String)
	 * @param id - review id to check
	 * @return Status.OK if user does not exist in database
	 */
	public Status checkReviewExists(String id) {
		Status status = Status.ERROR;
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkReviewExists(connection, id);
		}
		catch (SQLException e) {
			status = Status.CONNECTION_FAILED;
			System.err.println(e);
		}
		
		return status;
	}
	
	/**
	 * Adds a new review into the database.
	 *
	 * @param connection - DB connection
	 * @param reviewid - review id of hotel
	 * @param hotelid - hotel id for which review is given
	 * @param user - review user
	 * @param rating - rating for hotel
	 * @param isrecommended - recommendation for hotel
	 * @param title - review title
	 * @param reviewtext - review text
	 * @param reviewdate - date when review is given
	 * @return status.ok if adding new review is successful
	 */
	private Status addReview(Connection connection, String reviewid, String hotelid, String user, double rating, boolean isrecommended, String title, String reviewtext, String reviewdate) {
		
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(ADDREVIEW_SQL);
		) {
			statement.setString(1, reviewid);
			statement.setString(2, hotelid);
			statement.setString(3, user);
			statement.setDouble(4, rating);
			statement.setBoolean(5, isrecommended);
			statement.setString(6, title);
			statement.setString(7, reviewtext);
			statement.setString(8, reviewdate);
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
	 * Adds a new review into the database if the review does not already exist.
	 *
	 * @param reviewid - review id of hotel
	 * @param hotelid - hotel id for which review is given
	 * @param user - review user
	 * @param rating - rating for hotel
	 * @param isrecommended - recommendation for hotel
	 * @param title - review title
	 * @param reviewtext - review text
	 * @param reviewdate - date when review is given
	 * @return status.ok if adding new review is successful
	 */
	public Status addReview(String reviewid, String hotelid, String user, double rating, boolean isrecommended, String title, String reviewtext, String reviewdate) {
		Status status = Status.ERROR;
		System.out.println("Adding " + reviewid + ".");
		
		if (isBlank(reviewid) || isBlank(hotelid)) {
			status = Status.INVALID_REVIEW;
			System.out.println(status);
			return status;
		}
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkReviewExists(connection, reviewid);
			
			// if okay so far, try to insert new user
			if (status != Status.OK) {
				status = addReview(connection, reviewid, hotelid, user, rating, isrecommended, title, reviewtext, reviewdate);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Adds a new review into the database.
	 *
	 * @param connection - DB connection
	 * @param reviewid - review id of hotel
	 * @param rating - rating for hotel
	 * @param isrecommended - recommendation for hotel
	 * @param title - review title
	 * @param reviewtext - review text
	 * @return status.ok if adding new review is successful
	 */
	private Status updateReview(Connection connection, String reviewid, double rating, boolean isrecommended, String title, String reviewtext) {
		
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(UPDATEREVIEW_SQL);
		) {
			statement.setString(5, reviewid);
			statement.setDouble(3, rating);
			statement.setBoolean(4, isrecommended);
			statement.setString(1, title);
			statement.setString(2, reviewtext);
			statement.executeUpdate();
			status = Status.OK;
		}
		catch (SQLException ex) {
			System.out.println(ex);
			status = Status.SQL_EXCEPTION;
			System.err.println(ex.getMessage() + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Adds a new review into the database if the review does not already exist.
	 *
	 * @param reviewid - review id of hotel
	 * @param rating - rating for hotel
	 * @param isrecommended - recommendation for hotel
	 * @param title - review title
	 * @param reviewtext - review text
	 * @return status.ok if adding new review is successful
	 */
	public Status updateReview(String reviewid, double rating, boolean isrecommended, String title, String reviewtext) {
		Status status = Status.ERROR;
		System.out.println("Adding " + reviewid + ".");
		
		if (isBlank(reviewid)) {
			status = Status.INVALID_REVIEW;
			System.err.println(status);
			return status;
		}
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkReviewExists(connection, reviewid);
			
			// if okay then review exists, try to update review
			if (status == Status.OK) {
				status = updateReview(connection, reviewid, rating, isrecommended, title, reviewtext);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	
	/**
	 * Removes a review from the database if the review id is
	 * provided correctly.
	 *
	 * @param id - review id to remove
	 * @return status.OK if removal successful
	 */
	private Status removeReview(Connection connection, String id) {
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(DELETE_SQL);
		) {
			statement.setString(1, id);
			
			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_REVIEW;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			System.err.println(ex);
		}
		
		return status;
	}
	
	/**
	 * Removes a review from the database if review id is
	 * provided correctly.
	 *
	 * @param id - review id to remove
	 * @return Status.OK if removal successful
	 */
	public Status removeReview(String id) {
		Status status = Status.ERROR;
		
		System.out.println("Removing review " + id + ".");
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkReviewExists(connection, id);
			
			if(status == Status.OK) {
				status = removeReview(connection, id);
			}
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Used to get reviews by hotel id
	 * @param connection - database connection
	 * @param hotelid - hotel id
	 * @return reviews by hotel id from DB
	 */
	private ResultSet getReviewsByHotelId(Connection connection, String hotelid) {
		Status status = Status.ERROR;
		ResultSet result = null;
		try{
			
			PreparedStatement statement = connection.prepareStatement(REVIEWS_BY_HOTELID_SQL);
			statement.setString(1, hotelid);
			result = statement.executeQuery();
			
		} catch (SQLException e) {
			System.err.println(e);;
		}
		return result;
	}
	
	/**
	 * Used to get review by hotel id
	 * @param hotelid - hotel id
	 * @return review by hotel id from DB
	 */
	public TreeSet<HotelReview> getReviewsByHotelId(String hotelid) {
		try {
			Connection connection = db.getConnection();
			ResultSet results = getReviewsByHotelId(connection, hotelid);
			Set<HotelReview> reviews = new TreeSet<>(new HotelReviewsComparator());
			if (results != null) {
				while (results.next()) {
					HotelReview review = new HotelReview(results.getString(1), results.getString(2), results.getString(3), results.getInt(4), results.getBoolean(5), results.getString(6), results.getString(7), results.getString(8));
					reviews.add(review);
				}
				return (TreeSet<HotelReview>) reviews;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Used to get review by review id
	 * @param connection - database connection
	 * @param reviewid - review id
	 * @return review from DB
	 */
	private ResultSet getReviewByReviewId(Connection connection, String reviewid) {
		Status status = Status.ERROR;
		ResultSet result = null;
		try{
			
			//Connection connection = db.getConnection();
			
			PreparedStatement statement = connection.prepareStatement(REVIEW_BY_REVIEWID_SQL);
			statement.setString(1, reviewid);
			result = statement.executeQuery();
			
		} catch (SQLException e) {
			System.err.println(e);;
		}
		return result;
	}
	
	/**
	 * Used to get review by review id
	 * @param reviewid - review id
	 * @return review from DB
	 */
	public HotelReview getReviewByReviewId(String reviewid) {
		ResultSet results = null;
		Status status = Status.ERROR;
		
		System.out.println("Returning review " + reviewid + ".");
		
		try (Connection connection = db.getConnection();) {
			status = checkReviewExists(connection, reviewid);
			if(status == Status.OK) {
				results = getReviewByReviewId(connection, reviewid);
			}
			if (results.next()) {
				HotelReview review = new HotelReview(results.getString(1), results.getString(2), results.getString(3), results.getInt(4), results.getBoolean(5), results.getString(6), results.getString(7), results.getString(8));
				return review;
			}
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return null;
	}
	
	/**
	 * Used to get avg rating by hotel id
	 * @param connection - database connection
	 * @param hotelid - hotel id
	 * @return avg rating from DB
	 */
	private ResultSet getAvgRating(Connection connection, String hotelid) {
		Status status = Status.ERROR;
		ResultSet result = null;
		try{
			PreparedStatement statement = connection.prepareStatement(AVGRATING_SQL);
			statement.setString(1, hotelid);
			result = statement.executeQuery();
			
		} catch (SQLException e) {
			System.err.println(e);;
		}
		return result;
	}
	
	/**
	 * Used to get average rating by hotel id
	 * @param hotelid - hotel id
	 * @return avg rating from DB
	 */
	public double getAvgRating(String hotelid) {
		ResultSet results = null;
		Status status = Status.ERROR;
		
		System.out.println("Returning average rating of " + hotelid + ".");
		
		try (Connection connection = db.getConnection();) {
			//status = checkReviewExists(connection, hotelid);
			results = getAvgRating(connection, hotelid);
			
			if (results.next()) {
				return results.getDouble(1);
			}
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return 0;
	}
	
	/**
	 * Used to get reviews by user
	 * @param connection - database connection
	 * @param user - user who created reviews
	 * @return review from DB
	 */
	private ResultSet getReviewsByUser(Connection connection, String user) {
		Status status = Status.ERROR;
		ResultSet result = null;
		try {
			PreparedStatement statement = connection.prepareStatement(REVIEWS_BY_USER_SQL);
			statement.setString(1, user);
			result = statement.executeQuery();
			
		} catch (SQLException e) {
			System.err.println(e);;
		}
		return result;
	}
	
	/**
	 * Used to get reviews by user
	 * @param user - User who created reviews
	 * @return review from DB
	 */
	public TreeSet<HotelReview> getReviewsByUser(String user) {
		System.out.println("Returning all reviews by " + user + ".");
		try (Connection connection = db.getConnection();){
			ResultSet results = getReviewsByUser(connection, user);
			
			if (results != null) {
				Set<HotelReview> reviews = new TreeSet<>(new HotelReviewsComparator());
				while (results.next()) {
					HotelReview review = new HotelReview(results.getString(1), results.getString(2), results.getString(3), results.getInt(4), results.getBoolean(5), results.getString(6), results.getString(7), results.getString(8));
					reviews.add(review);
				}
				return (TreeSet<HotelReview>) reviews;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Removes all reviews for a user from the database if the username is
	 * provided correctly.
	 * @param connection - database connection
	 * @param user - reviews to remove by user
	 * @return status.OK if removal successful
	 */
	private Status removeAllReviewsByUser(Connection connection, String user) {
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(DELETE_REVIEWS_BY_USER_SQL);
		) {
			statement.setString(1, user);
			
			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			System.err.println(ex);
		}
		
		return status;
	}
	
	/**
	 * Removes all reviews for a user from the database if the username is
	 * provided correctly.
	 *
	 * @param user - reviews to remove by user
	 * @return status.OK if removal successful
	 */
	public Status removeAllReviewsByUser(String user) {
		Status status = Status.ERROR;
		
		System.out.println("Removing reviews by user " + user + ".");
		
		try (
				Connection connection = db.getConnection();
		) {
			status = removeAllReviewsByUser(connection, user);
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	
}
