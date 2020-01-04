package hotelapp;

import java.util.Comparator;

/** Implements comparator and sorts hotels reviews with most recent review on top.
 *  Used in TreeSet of hotel reviews.
 */

public class HotelReviewsComparator implements Comparator<HotelReview> {
	
	public int compare(HotelReview hr1, HotelReview hr2){
		return hr1.compareTo(hr2);
	}
}
