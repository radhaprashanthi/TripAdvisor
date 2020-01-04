package jettyServer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all DB table operations related to visited links
 * Performs create, update and delete operations.
 */
public class VisitedLinksDatabaseHandler {
	//private static Logger log = LogManager.getLogger();
	
	/** Makes sure only one database handler is instantiated. */
	private static VisitedLinksDatabaseHandler singleton = new VisitedLinksDatabaseHandler();
	
	/** Used to determine if necessary tables are provided. */
	private static final String TABLES_SQL =
			"SHOW TABLES LIKE 'visited_links';";
	
	/** Used to create visited_links table. */
	private static final String CREATE_SQL =
			"CREATE TABLE visited_links (id VARCHAR(64) NOT NULL, user VARCHAR(64) NOT NULL, PRIMARY KEY (id, user));";
	
	/** Used to insert a new visited link into the database. */
	private static final String SAVELINK_SQL =
			"INSERT INTO visited_links (id, user) " +
			"VALUES (?, ?);";
	
	/** Used to determine if a link is already saved. VisitedLinksDatabaseHandler*/
	private static final String CHECKLINK_SQL =
			"SELECT id FROM visited_links WHERE id = ? AND user = ?";
	
	
	/** Used to remove a link from the database. */
	private static final String DELETE_SQL =
			"DELETE FROM visited_links WHERE id = ? AND user = ?";
	
	/** Used to get all links visited by a user from the database. */
	private static final String LINKSVISITED_BY_USER_SQL =
			"SELECT id FROM visited_links WHERE user = ?";
	
	/** Used to get visited links by user from the database. */
	private static final String DELETE_ALLVISITEDLINKS_BY_USER_SQL =
			"DELETE FROM visited_links WHERE user = ?";
	
	
	/** Used to configure connection to database. */
	private DatabaseConnector db;
	
	/**
	 * Initializes a database handler for the visited links. Private constructor
	 * forces all other classes to use singleton.
	 */
	private VisitedLinksDatabaseHandler() {
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
			System.err.println(status.message());
		}
	}
	
	/**
	 * Gets the single instance of the database handler.
	 *
	 * @return instance of the database handler
	 */
	public static VisitedLinksDatabaseHandler getInstance() {
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
	 * Checks if a link already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection - active database connection
	 * @param id - hotel id
	 * @param user - username
	 * @return Status.OK if link exists in database
	 * @throws SQLException
	 */
	private Status checkIfLinkIsVisited(Connection connection, String id, String user) {
		
		assert connection != null;
		assert id != null;
		
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(CHECKLINK_SQL);
		) {
			statement.setString(1, id);
			statement.setString(2, user);
			
			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.DUPLICATE_LINK : Status.OK;
		}
		catch (SQLException e) {
			System.err.println(status + " " + e);
			status = Status.SQL_EXCEPTION;
		}
		
		return status;
	}
	
	/**
	 * Tests if a link already exists in the database.
	 *
	 * @see #checkIfLinkIsVisited(Connection, String, String)
	 * @param id - hotel id to check
	 * @return Status.OK if user does not exist in database
	 */
	public Status checkIfLinkIsVisited(String id, String user) {
		Status status = Status.ERROR;
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkIfLinkIsVisited(connection, id, user);
		}
		catch (SQLException e) {
			status = Status.CONNECTION_FAILED;
			System.err.println(e.getMessage() + " " + e);
		}
		
		return status;
	}
	
	/**
	 * Adds a new link, placing the hotel id and username
	 * into the database if the hotel id and user does not already exist.
	 *
	 * @param id - id of hotel
	 * @param user - name of the logged in user
	 * @return status ok if link addition is successful
	 */
	private Status saveLink(Connection connection, String id, String user) {
		
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(SAVELINK_SQL);
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
	 * Adds a new link, placing the hotel id and username
	 * into the database if the hotel id and user does not already exist.
	 *
	 * @param id - id of hotel
	 * @param user - name of the logged in user
	 * @return status ok if link addition is successful
	 */
	public Status saveLink(String id, String user) {
		Status status = Status.ERROR;
		System.out.println("Adding " + id + ".");
		
		// make sure we have non-null and non-emtpy values for hotel
		if (isBlank(id) || isBlank(user)) {
			status = Status.INVALID_LINK;
			System.err.println(status);
			return status;
		}
		
		// try to connect to database and test for duplicate user
		System.out.println(db);
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkIfLinkIsVisited(connection, id, user);
			
			// if okay so far, try to insert new user
			if (status == Status.OK) {
				status = saveLink(connection, id, user);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Removes a visitied link from the database if the hotel id and user are
	 * provided correctly.
	 * @param connection - Database connection
	 * @param id - hotel id to remove
	 * @param user - user to remove
	 * @return status.OK if removal successful
	 */
	private Status removeVisitedLink(Connection connection, String id, String user) {
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
	 * Removes a visited link from the database if hotel id and user are
	 * provided correctly.
	 *
	 * @param id - hotel id to remove
	 * @param user - username of hotel
	 * @return Status.OK if removal successful
	 */
	public Status removeVisitedLink(String id, String user) {
		Status status = Status.ERROR;
		
		System.out.println("Removing saved link " + id + ".");
		
		try (
				Connection connection = db.getConnection();
		) {
			status = checkIfLinkIsVisited(connection, id, user);
			
			if(status != Status.OK) {
				status = removeVisitedLink(connection, id, user);
			}
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Used to get all the links visited by the user
	 * @param connection - Database connection
	 * @param user - logged in user
	 * @return Results of links visited by user from DB
	 */
	private ResultSet getLinksVisitedByUser(Connection connection, String user) {
		Status status = Status.ERROR;
		ResultSet results = null;
		try {
			PreparedStatement statement = connection.prepareStatement(LINKSVISITED_BY_USER_SQL);
			statement.setString(1, user);
			results = statement.executeQuery();
			
		} catch (SQLException e) {
			System.err.println(e);
		}
		return results;
	}
	
	/**
	 * Get all the saved links for a user
	 * @param user - logged in user
	 * @return list of links by user in database
	 */
	public List getLinksVisitedByUser(String user) {
		try (Connection connection = db.getConnection();) {
			ResultSet results = getLinksVisitedByUser(connection, user);
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
	 * Removes all links visited by user from the database if the user is
	 * provided correctly.
	 * @param connection - Database connection
	 * @param user - username to remove
	 * @return status.OK if removal successful
	 */
	private Status clearAllLinksVisitedByUser(Connection connection, String user) {
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(DELETE_ALLVISITEDLINKS_BY_USER_SQL);
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
	 * Removes all links visited by user from the database if the user is
	 * provided correctly.
	 *
	 * @param user - username to remove
	 * @return status.OK if removal successful
	 */
	public Status clearAllLinksVisitedByUser(String user) {
		Status status = Status.ERROR;
		
		System.out.println("Removing saved hotel by " + user + ".");
		
		try (
				Connection connection = db.getConnection();
		) {
			status = clearAllLinksVisitedByUser(connection, user);
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
}
