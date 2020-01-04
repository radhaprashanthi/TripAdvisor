package jettyServer;

/**
 * Enum to store custom status and messages.
 */
public enum Status {

	/*
	 * Creates several Status enum types. The Status name and message is
	 * given in the NAME(message) format below. The Status ordinal is
	 * determined by its position in the list. (For example, OK is the
	 * first element, and will have ordinal 0.)
	 */

	OK("No errors occured."),
	ERROR("Unknown error occurred."),
	MISSING_CONFIG("Unable to find configuration file."),
	MISSING_VALUES("Missing values in configuration file."),
	CONNECTION_FAILED("Failed to establish a database connection."),
	CREATE_FAILED("Failed to create necessary tables."),
	INVALID_LOGIN("Invalid username and/or password."),
	INVALID_USER("User does not exist."),
	DUPLICATE_USER("User with that username already exists."),
	SQL_EXCEPTION("Unable to execute SQL statement."),
	INVALID_HOTEL("Invalid hotel id and/or name."),
	DUPLICATE_HOTEL("Hotel with that id already exists."),
	DUPLICATE_SAVEHOTEL("Hotel is already saved."),
	INVALID_LINK("Invalid user name/hotel id."),
	DUPLICATE_LINK("Link was visited before."),
	INVALID_REVIEW("Invalid review/hotel id."),
	INVALID_SAVEHOTEL("Invalid hotel or user."),
	DUPLICATE_REVIEW("Review with that id already exists."),
	INVALID_PASSWORD("Password must contain at least one number, letter and special character {@#$%})"),
	INVALID_PASSWORD_LENGTH("Password must be at least 5 and not more than 10 characters long");
	
	private final String message;
	
	/** Set status message */
	private Status(String message) {
		this.message = message;
	}

	/** Get message for status */
	public String message() {
		return message;
	}

	/** return message of status*/
	@Override
	public String toString() {
		return this.message;
	}
}