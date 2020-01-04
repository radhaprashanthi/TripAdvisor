package hotelapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**Stores single review details for a hotel.
 * Overrides compareTo method to sort reviews by
 * review date (recent ones on top),user nickname (alphabetical order) and review id.
 */
public class HotelReview implements Comparable<HotelReview> {
	
	private String hotelId;
	private String reviewId;
	private int rating;
	private String title;
	private String reviewText;
	private String user;
	private Date date;
	private boolean isRecommended;
	
	/**
	 * Constructor of this class
	 * @param hotelId - id of hotel
	 * @param reviewId - review of id
	 * @param rating - rating of hotel
	 * @param title - review title
	 * @param reviewText - review
	 * @param isRecom - recommendation of hotel as YES/NO
	 * @param reviewDate - date of review
	 * @param userNickname - name of the user who gave hotel review
	 */
	public HotelReview(String reviewId, String hotelId, String userNickname, int rating, boolean isRecom, String title, String reviewText, String reviewDate) {
		this.hotelId = hotelId;
		this.reviewId = reviewId;
		this.rating = rating;
		this.title = title;
		this.reviewText = reviewText;
		this.user = userNickname;
		this.isRecommended = isRecom;
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		try {
			this.date = format.parse(reviewDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/** Returns review id of the hotel */
	public String getReviewId() {
		return reviewId;
	}
	
	/** Returns userNickname who gave review for the hotel */
	public String getUserNickname() {
		return user;
	}
	
	/** Returns date and time of the review */
	public Date getReviewDate() {
		return date;
	}
	
	/** Returns title of the review */
	public String getTitle() {
		return title;
	}
	
	/** Returns text of the review */
	public String getReviewText() {
		return reviewText;
	}
	
	/** Returns hotel id */
	public String getHotelId() {
		return hotelId;
	}
	
	/** Returns hotel rating */
	public int getRating() {
		return rating;
	}
	
	/** Returns hotel recommendation */
	public boolean isRecommended() {
		return isRecommended;
	}
	
	/** Sorts review dates in reverse chronological order.
	 * If review dates are same compares with sorts based on user nicknames.
	 * If user nicknames are also same, then sorts based on review id.
	 *
	 * @param review takes review object to compare to
	 * @return integer is returned based on <, =, >
	 */
	@Override
	public int compareTo(HotelReview review) {
		int result;
		if (this.date.after(review.date)) {
			result = -1;
		} else if (this.date.equals(review.date)) {
			if (this.user.compareTo(review.user) == 0) {
				result = reviewId.compareTo(review.reviewId);
			} else {
				result = this.user.compareTo(review.user);
			}
		} else {
			result = 1;
		}
		return result;
	}
	
	/**
	 * Returns string about hotel review.
	 * @return - Hotel review information string.
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("--------------------");
		sb.append(System.lineSeparator());
		sb.append("Review by ");
		sb.append(this.user);
		sb.append(" on ");
		sb.append(this.date);
		sb.append(System.lineSeparator());
		sb.append("Rating: ");
		sb.append(this.rating);
		sb.append(System.lineSeparator());
		sb.append(this.title);
		sb.append(System.lineSeparator());
		sb.append(this.reviewText);
		sb.append(System.lineSeparator());
		
		return sb.toString();
	}
}
