package jettyServer;

import hotelapp.HotelReview;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Reviews servlet to interact with UI servlet and Database to
 * fetch reviews data from DB and use it in UI.
 */
public class ReviewBaseServlet {
	
	/** Review handler to interact with database table*/
	protected static final ReviewDatabaseHandler dbhandler = ReviewDatabaseHandler.getInstance();
	
	/**
	 * Get reviews by hotel id
	 * @param hotelid - hotel id
	 * @return list of hotels reviews
	 */
	protected List<HotelReview> getReviewsByHotelId (String hotelid) {
		TreeSet<HotelReview> reviewsSet = dbhandler.getReviewsByHotelId(hotelid);
		List<HotelReview> reviews = new ArrayList<>();
		if (reviewsSet != null) {
			for (HotelReview review: reviewsSet) {
				reviews.add(review);
			}
			return reviews;
		}
		return null;
	}
	
	/**
	 * Add review to DB
	 * @param reviewid - review id
	 * @param hotelid - hotel id
	 * @param user - user name
	 * @param rating - rating for hotel
	 * @param isrecommended - recommended by user
	 * @param title - review title
	 * @param reviewtext - review text
	 * @param reviewdate - review date
	 * @return status OK id review successfully added to DB
	 */
	protected Status addReview(String reviewid, String hotelid, String user, double rating, boolean isrecommended, String title, String reviewtext, String reviewdate) {
		return dbhandler.addReview(reviewid, hotelid, user, rating, isrecommended, title, reviewtext, reviewdate);
	}
	
	/**
	 * Status message based on code id
	 * @param code status code id
	 * @return status message
	 */
	protected String getStatusMessage(int code) {
		Status status = null;
		
		try {
			status = Status.values()[code];
		}
		catch (Exception ex) {
			System.err.println(ex.getMessage() + ex);
			status = Status.ERROR;
		}
		
		return status.toString();
	}
	
	/**
	 * Delete review from DB
	 * @param reviewId - review Id
	 * @return status OK when successfully deleted
	 */
	protected Status deleteReview(String reviewId) {
		return dbhandler.removeReview(reviewId);
	}
	
	/**
	 * Update review
	 * @param reviewid - review id
	 * @param rating - rating
	 * @param isrecommended - recommendation
	 * @param title - review title
	 * @param reviewtext - review text
	 * @return status OK if successfully updated
	 */
	protected Status updateReview(String reviewid, double rating, boolean isrecommended, String title, String reviewtext) {
		return dbhandler.updateReview(reviewid, rating, isrecommended, title, reviewtext);
	}
	
	/**
	 * Review by review id
	 * @param reviewId - review id
	 * @return hotel review
	 */
	protected HotelReview getReviewByReviewId(String reviewId) {
		return dbhandler.getReviewByReviewId(reviewId);
	}
	
	/**
	 * Get average rating for a hotel
	 * @param hotelId - hotel id
	 * @return average rating for a hotel
	 */
	protected double getAvgRating(String hotelId) {
		return dbhandler.getAvgRating(hotelId);
	}
	
	/**
	 * Sort reviews by review date, review id and username
	 * @param reviews - reviews TreeSet
	 * @return sorted reviews TreeSet
	 */
	private TreeSet<HotelReview> sortReviews(TreeSet<HotelReview> reviews){
		Set<HotelReview> sortedReviews = new TreeSet<>((hr1, hr2) -> {
			
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
		for (HotelReview review: reviews) {
			sortedReviews.add(review);
		}
		return (TreeSet<HotelReview>) sortedReviews;
	}
	
	/**
	 * Get reviews by user
	 * @param user - user name
	 * @return hotel reviews
	 */
	protected List<HotelReview> getReviewsByUser (String user) {
		TreeSet<HotelReview> reviewsSet = dbhandler.getReviewsByUser(user);
		List<HotelReview> reviews = new ArrayList<>();
		if (reviewsSet != null) {
			for (HotelReview review: reviewsSet) {
				reviews.add(review);
			}
			return reviews;
		}
		return null;
	}
	
	/**
	 * Delete all reviews by user
	 * @param user - user name
	 * @return status OK if all reviews successfully deleted
	 */
	protected Status deleteAllReviewsByUser(String user) {
		return dbhandler.removeAllReviewsByUser(user);
	}
}
