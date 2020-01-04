package jettyServer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles all login database table operations like create, update, delete
 * Also handles other queries like authenticate user, register user, login
 * Save/Update last login
 */
public class LoginDatabaseHandler {
	
	//private static Logger log = LogManager.getLogger();
	
	/** Makes sure only one database handler is instantiated. */
	private static LoginDatabaseHandler singleton = new LoginDatabaseHandler();
	
	/** Used to determine if necessary tables are provided. */
	private static final String TABLES_SQL =
			"SHOW TABLES LIKE 'login_users';";
	
	/** Used to create necessary tables for this example. */
	private static final String CREATE_SQL =
			"CREATE TABLE login_users (" +
			"userid INTEGER AUTO_INCREMENT PRIMARY KEY, " +
			"username VARCHAR(32) NOT NULL UNIQUE, " +
			"password CHAR(64) NOT NULL, " +
			"usersalt CHAR(32) NOT NULL, " +
			"lastlogin VARCHAR(256), " +
			"currentlogin VARCHAR(256));";
	
	/** Used to insert a new user into the database. */
	private static final String REGISTER_SQL =
			"INSERT INTO login_users (username, password, usersalt) " +
			"VALUES (?, ?, ?);";
	
	/** Used to determine if a username already exists. */
	private static final String USER_SQL =
			"SELECT username, lastlogin, currentlogin FROM login_users WHERE username = ?";
	
	/** Used to retrieve the salt associated with a specific user. */
	private static final String SALT_SQL =
			"SELECT usersalt FROM login_users WHERE username = ?";
	
	/** Used to authenticate a user. */
	private static final String AUTH_SQL =
			"SELECT username FROM login_users " +
			"WHERE username = ? AND password = ?";
	
	/** Used to remove a user from the database. */
	private static final String DELETE_SQL =
			"DELETE FROM login_users WHERE username = ?";
	
	/** Used to update last and current login date of user into the database. */
	private static final String UPDATE_LOGIN_DATETIME_SQL =
			"UPDATE login_users " +
			"SET lastlogin = ? , currentlogin = ? " +
			"WHERE username = ?";
	
	
	/** Used to configure connection to database. */
	private DatabaseConnector db;
	
	/** Used to generate password hash salt for user. */
	private Random random;
	
	/**
	 * Initializes a database handler for the Login example. Private constructor
	 * forces all other classes to use singleton.
	 */
	private LoginDatabaseHandler() {
		Status status = Status.OK;
		random = new Random(System.currentTimeMillis());
		
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
	 *
	 * @return instance of the database handler
	 */
	public static LoginDatabaseHandler getInstance() {
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
	 * Tests if a user already exists in the database. Requires an active
	 * database connection.
	 *
	 * @param connection - active database connection
	 * @param user - username to check
	 * @return Status.OK if user does not exist in database
	 * @throws SQLException
	 */
	private Status duplicateUser(Connection connection, String user) {
		
		assert connection != null;
		assert user != null;
		
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(USER_SQL);
		) {
			statement.setString(1, user);
			
			ResultSet results = statement.executeQuery();
			status = results.next() ? Status.DUPLICATE_USER : Status.OK;
		}
		catch (SQLException e) {
			System.err.println(e.getMessage() + " " + e);
			status = Status.SQL_EXCEPTION;
		}
		
		return status;
	}
	
	/**
	 * Tests if a user already exists in the database.
	 *
	 * @see #duplicateUser(Connection, String)
	 * @param user - username to check
	 * @return Status.OK if user does not exist in database
	 */
	public Status duplicateUser(String user) {
		Status status = Status.ERROR;
		
		try (
				Connection connection = db.getConnection();
		) {
			status = duplicateUser(connection, user);
		}
		catch (SQLException e) {
			status = Status.CONNECTION_FAILED;
			System.err.println(e.getMessage() + " " +  e);
		}
		
		return status;
	}
	
	/**
	 * Returns the hex encoding of a byte array.
	 *
	 * @param bytes - byte array to encode
	 * @param length - desired length of encoding
	 * @return hex encoded byte array
	 */
	public static String encodeHex(byte[] bytes, int length) {
		BigInteger bigint = new BigInteger(1, bytes);
		String hex = String.format("%0" + length + "X", bigint);
		
		assert hex.length() == length;
		return hex;
	}
	
	/**
	 * Calculates the hash of a password and salt using SHA-256.
	 *
	 * @param password - password to hash
	 * @param salt - salt associated with user
	 * @return hashed password
	 */
	public static String getHash(String password, String salt) {
		String salted = salt + password;
		String hashed = salted;
		
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(salted.getBytes());
			hashed = encodeHex(md.digest(), 64);
		}
		catch (Exception ex) {
			System.err.println("Unable to properly hash password." + " " +  ex);
		}
		
		return hashed;
	}
	
	/**
	 * Registers a new user, placing the username, password hash, and
	 * salt into the database if the username does not already exist.
	 *
	 * @param newuser - username of new user
	 * @param newpass - password of new user
	 * @return status ok if registration successful
	 */
	private Status registerUser(Connection connection, String newuser, String newpass) {
		
		Status status = Status.ERROR;
		
		byte[] saltBytes = new byte[16];
		random.nextBytes(saltBytes);
		
		String usersalt = encodeHex(saltBytes, 32);
		String passhash = getHash(newpass, usersalt);
		
		try (
				PreparedStatement statement = connection.prepareStatement(REGISTER_SQL);
		) {
			statement.setString(1, newuser);
			statement.setString(2, passhash);
			statement.setString(3, usersalt);
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
	 * Registers a new user, placing the username, password hash, and
	 * salt into the database if the username does not already exist.
	 * Does password validation using regex.
	 *
	 * @param newuser - username of new user
	 * @param newpass - password of new user
	 * @return status.ok if registration successful
	 */
	public Status registerUser(String newuser, String newpass) {
		Status status = Status.ERROR;
		System.out.println("Registering " + newuser + ".");
		
		Pattern p = Pattern.compile("(?=.*\\d)(?=.*[a-zA-Z])(?=.*[@$%#]){5,10}");
		Matcher m = p.matcher(newpass);
		if (!m.find()) {
			status = Status.INVALID_PASSWORD;
			return status;
		}
		if (newpass.length() < 5 || newpass.length() > 10) {
			status = Status.INVALID_PASSWORD_LENGTH;
			return status;
		}
		// make sure we have non-null and non-emtpy values for login
		if (isBlank(newuser) || isBlank(newpass)) {
			status = Status.INVALID_LOGIN;
			return status;
		}
		
		// try to connect to database and test for duplicate user
		System.out.println(db);
		
		try (
				Connection connection = db.getConnection();
		) {
			status = duplicateUser(connection, newuser);
			
			// if okay so far, try to insert new user
			if (status == Status.OK) {
				status = registerUser(connection, newuser, newpass);
			}
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Gets the salt for a specific user.
	 *
	 * @param connection - active database connection
	 * @param user - which user to retrieve salt for
	 * @return salt for the specified user or null if user does not exist
	 * @throws SQLException if any issues with database connection
	 */
	private String getSalt(Connection connection, String user) throws SQLException {
		assert connection != null;
		assert user != null;
		
		String salt = null;
		
		try (
				PreparedStatement statement = connection.prepareStatement(SALT_SQL);
		) {
			statement.setString(1, user);
			
			ResultSet results = statement.executeQuery();
			
			if (results.next()) {
				salt = results.getString("usersalt");
			}
		}
		
		return salt;
	}
	
	/**
	 * Checks if the provided username and password match what is stored
	 * in the database. Requires an active database connection.
	 * @param connection - database connection
	 * @param username - username to authenticate
	 * @param password - password to authenticate
	 * @return status.ok if authentication successful
	 * @throws SQLException
	 */
	private Status authenticateUser(Connection connection, String username,
	                                String password) throws SQLException {
		
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(AUTH_SQL);
		) {
			String usersalt = getSalt(connection, username);
			String passhash = getHash(password, usersalt);
			
			statement.setString(1, username);
			statement.setString(2, passhash);
			
			ResultSet results = statement.executeQuery();
			status = results.next() ? status = Status.OK : Status.INVALID_LOGIN;
		}
		catch (SQLException e) {
			System.err.println(e.getMessage() + " " + e);
			status = Status.SQL_EXCEPTION;
		}
		
		return status;
	}
	
	/**
	 * Checks if the provided username and password match what is stored
	 * in the database. Must retrieve the salt and hash the password to
	 * do the comparison.
	 *
	 * @param username - username to authenticate
	 * @param password - password to authenticate
	 * @return status.ok if authentication successful
	 */
	public Status authenticateUser(String username, String password) {
		Status status = Status.ERROR;
		
		System.out.println("Authenticating user " + username + ".");
		
		try (
				Connection connection = db.getConnection();
		) {
			status = authenticateUser(connection, username, password);
		}
		catch (SQLException ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 * @param connection - database connection
	 * @param username - username to remove
	 * @param password - password of user
	 * @return status.OK if removal successful
	 */
	private Status removeUser(Connection connection, String username, String password) {
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(DELETE_SQL);
		) {
			statement.setString(1, username);
			
			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Removes a user from the database if the username and password are
	 * provided correctly.
	 *
	 * @param username - username to remove
	 * @param password - password of user
	 * @return Status.OK if removal successful
	 */
	public Status removeUser(String username, String password) {
		Status status = Status.ERROR;
		
		System.out.println("Removing user " + username + ".");
		
		try (
				Connection connection = db.getConnection();
		) {
			status = authenticateUser(connection, username, password);
			
			if(status == Status.OK) {
				status = removeUser(connection, username, password);
			}
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Add last and current login of a user to the database
	 * @param connection - database connection
	 * @param lastlogin - last login of user
	 * @param currentlogin - current login of user
	 * @param username - username of logged in user
	 * @return status.OK if removal successful
	 */
	private Status updateLastLogin(Connection connection, String lastlogin, String currentlogin, String username) {
		Status status = Status.ERROR;
		
		try (
				PreparedStatement statement = connection.prepareStatement(UPDATE_LOGIN_DATETIME_SQL);
		) {
			statement.setString(1, lastlogin);
			statement.setString(2, currentlogin);
			statement.setString(3, username);
			
			int count = statement.executeUpdate();
			status = (count == 1) ? Status.OK : Status.INVALID_USER;
		}
		catch (SQLException ex) {
			status = Status.SQL_EXCEPTION;
			System.err.println(status + " " + ex);
		}
		
		return status;
	}
	
	/**
	 * Get last login of a user to the database
	 * @param username - username of logged in user
	 * @return Last login date
	 */
	public String getLastLogin(String username) {
		Status status = Status.ERROR;
		String lastLogin = null;
		
		System.out.println("Returning last login of user " + username + ".");
		
		try (
				Connection connection = db.getConnection();
		) {
			lastLogin = getLastLogin(connection, username);
			if (lastLogin ==  null) {
				String format = "hh:mm a 'on' EEE, MMM dd, yyyy";
				DateFormat dateFormat = new SimpleDateFormat(format);
				lastLogin = "First login at " + dateFormat.format(Calendar.getInstance().getTime());
			}
			else lastLogin = "Last logged in at " + lastLogin;
			
		}
		catch (Exception ex) {
			status = Status.CONNECTION_FAILED;
			System.err.println(status + " " + ex);
		}
		return lastLogin;
	}
	
	/**
	 * Add last and current login of a user to the database
	 * @param currentlogin - current login of user
	 * @param user - username of logged in user
	 * @return status.OK if removal successful
	 */
	public Status updateLastLogin(String currentlogin, String user) {
		
		assert user != null;
		ResultSet results = null;
		String lastlogin = null;
		String currentloginDB = null;
		Status status = Status.ERROR;
		
		try (   Connection connection = db.getConnection();
				PreparedStatement statement = connection.prepareStatement(USER_SQL);
		) {
			statement.setString(1, user);
			results = statement.executeQuery();
			if (results.next()) {
				lastlogin = results.getString(2);
				currentloginDB = results.getString(3);
			}
			lastlogin = currentloginDB;
			status = updateLastLogin(connection, lastlogin, currentlogin, user);
		}
		catch (SQLException e) {
			System.err.println(e.getMessage() + " " + e);
		}
		return status;
		
	}
	
	/**
	 * Get last login for a user
	 * @param connection - database connection
	 * @param user - user name
	 * @return last login date is returned
	 */
	private String getLastLogin(Connection connection, String user) {
		
		assert connection != null;
		assert user != null;
		ResultSet results;
		String lastLogin = null;
		
		try (
				PreparedStatement statement = connection.prepareStatement(USER_SQL);
		) {
			statement.setString(1, user);
			results = statement.executeQuery();
			if (results.next()) {
				lastLogin = results.getString(2);
			}
		}
		catch (SQLException e) {
			System.err.println(e.getMessage() + " " + e);
		}
		return lastLogin;
		
	}
	
	
}
