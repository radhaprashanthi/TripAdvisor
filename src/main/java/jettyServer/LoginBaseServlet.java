package jettyServer;

import javax.servlet.http.HttpServlet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Provides base functionality to all Login servlets
 * like welcome, register, login, redirect.
 */
@SuppressWarnings("serial")
public class LoginBaseServlet extends HttpServlet {

	//protected static Logger log = LogManager.getLogger();
	/** Database handler for login */
	protected static final LoginDatabaseHandler dbhandler = LoginDatabaseHandler.getInstance();
	
	/** Get date in required format */
	protected String getDate() {
		String format = "hh:mm a 'on' EEE, MMM dd, yyyy";
		DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(Calendar.getInstance().getTime());
	}
	
	/** Get status message */
	protected String getStatusMessage(String errorName) {
		Status status = null;

		try {
			status = Status.valueOf(errorName);
		}
		catch (Exception ex) {
			System.err.println(errorName + " " + ex);
			status = Status.ERROR;
		}

		return status.toString();
	}

	/** Get error status message code*/
	protected String getStatusMessage(int code) {
		Status status = null;

		try {
			status = Status.values()[code];
		}
		catch (Exception ex) {
			System.err.println(ex.getMessage() + " " + ex);
			status = Status.ERROR;
		}

		return status.toString();
	}
	
	/** Get last login date */
	protected String getLastLogin(String username) {
		return dbhandler.getLastLogin(username);
	}
	
	/** Update last login date time in DB */
	protected Status updateLastLogin(String currentLogin, String username) {
		return dbhandler.updateLastLogin(currentLogin, username);
	}
}