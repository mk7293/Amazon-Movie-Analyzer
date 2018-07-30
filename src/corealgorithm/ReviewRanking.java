package corealgorithm;
/**
 * ReviewRanking.java
 * 
 * Date: 7/22/2018
 * 
 * 
 */
import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class ReviewRanking {

	HashMap<String, ArrayList<ReviewDetails>> reviewMap;
	ArrayList<ReviewDetails> positiveReviews = new ArrayList<>();
	ArrayList<ReviewDetails> negativeReviews = new ArrayList<>();
	ArrayList<String> dates = new ArrayList<>();
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/DD/YYYY");

	public ReviewRanking(String csvFile) {
		reviewMap = new HashMap<>();
		FileReader in;
		int i = 0;
		try {
			in = new FileReader(csvFile);
			for (CSVRecord csv : CSVFormat.EXCEL.parse(in)) {
				if (i == 0) {
					i = 1;
					continue;
				}

				String movieId = csv.get(4);
				String reviewText = csv.get(5);
				int reviewRating = Integer.parseInt(csv.get(3));
				String reviewDate = csv.get(6);
				int sentimentScore = Integer.parseInt(csv.get(7));

				ReviewDetails details = new ReviewDetails(reviewText, reviewRating, reviewDate, sentimentScore,
						movieId);

				if (reviewMap.containsKey(movieId)) {
					ArrayList<ReviewDetails> list = reviewMap.get(movieId);
					list.add(details);
					reviewMap.put(movieId, list);
				} else {
					ArrayList<ReviewDetails> list = new ArrayList<>();
					list.add(details);
					reviewMap.put(movieId, list);
				}

			}
		} catch (FileNotFoundException e) {
			System.err.println("CSV File Not Found");
		} catch (IOException e) {
			System.err.println("IO Exception Occured");
		}
	}

	private void assignReviewScore(ReviewDetails details) throws ParseException {
		int sentimentScore = details.sentimentScore * 7;
		int ratingScore = details.reviewRating * 8;
		int dateScore;
		Date currentDate = new SimpleDateFormat("MM/DD/YYYY").parse("07/27/2018");

		@SuppressWarnings("deprecation")
		long months = (currentDate.getYear() - details.reviewDate.getYear()) * 12
				+ (currentDate.getMonth() - details.reviewDate.getMonth());

		if (months <= 24) {
			dateScore = 32;
		} else if (months > 24 && months <= 48) {
			dateScore = 24;
		} else if (months > 48 && months <= 72) {
			dateScore = 16;
		} else if (months > 72 && months <= 100) {
			dateScore = 8;
		} else {
			dateScore = 0;
		}

		details.reviewScore = sentimentScore + ratingScore + dateScore;

		if (details.reviewScore > 50) {
			positiveReviews.add(details);
		} else {
			negativeReviews.add(details);
		}
	}

	private ArrayList<ArrayList<ReviewDetails>> retrieveReviews(String bookId) throws ParseException {

		if (!reviewMap.containsKey(bookId)) {
			return null;
		}

		ArrayList<ArrayList<ReviewDetails>> reviews = new ArrayList<>();
		ArrayList<ReviewDetails> details = reviewMap.get(bookId);

		for (ReviewDetails reviewDetails : details) {
			assignReviewScore(reviewDetails);
		}

		Collections.sort(positiveReviews, new DetailsComparator());
		Collections.sort(negativeReviews, new DetailsComparator());

		ArrayList<ReviewDetails> posReviews = new ArrayList<>();
		ArrayList<ReviewDetails> negReviews = new ArrayList<>();

		int i = 0;
		int posMax = 10;
		if (positiveReviews.size() < 10) {
			posMax = positiveReviews.size();
		}
		int negMax = 10;
		if (negativeReviews.size() < 10) {
			negMax = negativeReviews.size();
		}
		while (i < posMax) {
			dates.add(dateFormat.format(positiveReviews.get(i).reviewDate));
			posReviews.add(positiveReviews.get(i));
			i++;
		}
		i = 0;
		while (i < negMax) {
			dates.add(dateFormat.format(negativeReviews.get(i).reviewDate));
			negReviews.add(negativeReviews.get(i));
			i++;
		}

		reviews.add(posReviews);
		reviews.add(negReviews);

		return reviews;
	}

	public static void main(String[] args) throws ParseException {
		ReviewRanking ranking = new ReviewRanking("OverAll50K.csv");

		ArrayList<ArrayList<ReviewDetails>> reviews = ranking.retrieveReviews("B00005JLYQ");

		try {
			System.out.println("Positive Reviews are: ");
			ArrayList<ReviewDetails> detailsList = reviews.get(0);
			for (ReviewDetails reviewDetails : detailsList) {
				System.out.println(reviewDetails.movieId + "    ::     " + reviewDetails.reviewText.substring(0, 50)
						+ " :: " + reviewDetails.reviewRating + "  ::   " + reviewDetails.reviewDate + "  ::  " + reviewDetails.reviewScore);
			}

			System.out.println("Negative Reviews are: ");
			ArrayList<ReviewDetails> detailsList1 = reviews.get(1);
			for (ReviewDetails reviewDetails : detailsList1) {
				System.out.println(reviewDetails.movieId + "    ::     " + reviewDetails.reviewText.substring(0, 50)
						+ " :: " + reviewDetails.reviewRating + "  ::   " + reviewDetails.reviewDate + "  ::  "  + reviewDetails.reviewScore);
			}

		} catch (NullPointerException e) {
			System.err.println("Movie/TV not Found!");
		}
	}

}

class ReviewDetails {
	String reviewText;
	int reviewRating;
	Date reviewDate;
	double reviewScore;
	int sentimentScore;
	String movieId;

	public ReviewDetails(String reviewText, int reviewRating, String date, int sentimentScore, String movieId) {
		this.reviewText = reviewText;
		this.reviewRating = reviewRating;
		this.reviewScore = 0.0;
		this.sentimentScore = sentimentScore;
		this.movieId = movieId;
		try {
			this.reviewDate = (Date) new SimpleDateFormat("mm dd,yyyy").parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
}

class DetailsComparator implements Comparator<ReviewDetails> {
	@Override
	public int compare(ReviewDetails rd1, ReviewDetails rd2) {
		return rd1.reviewScore > rd2.reviewScore ? -1 : (rd1.reviewScore == rd2.reviewScore ? 0 : 1);
	}
}
