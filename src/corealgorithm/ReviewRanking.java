package corealgorithm;

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
					continue;
				}

				// TO-DO Figure the column index values
				String movieId = csv.get(i);
				String reviewText = csv.get(i);
				int reviewRating = Integer.parseInt(csv.get(i));
				String reviewDate = csv.get(i);
				int sentimentScore = Integer.parseInt(csv.get(i));

				ReviewDetails details = new ReviewDetails(reviewText, reviewRating, reviewDate, sentimentScore);

				if (reviewMap.containsKey(movieId)) {
					ArrayList<ReviewDetails> list = reviewMap.get(movieId);
					list.add(details);
					reviewMap.put(movieId, list);
				} else {
					ArrayList<ReviewDetails> list = new ArrayList<>();
					list.add(details);
					reviewMap.put(movieId, list);
				}

				i++;
			}
		} catch (FileNotFoundException e) {
			System.err.println("CSV File Not Found");
		} catch (IOException e) {
			System.err.println("IO Exception Occured");
		}
	}

	private void assignReviewScore(ReviewDetails details) throws ParseException {
		int sentimentScore = details.sentimentScore * 10;
		int ratingScore = details.reviewRating * 8;
		int dateScore;
		Date currentDate = new SimpleDateFormat("MM/DD/YYYY").parse(String.valueOf(new Date()));

		@SuppressWarnings("deprecation")
		long months = (currentDate.getYear() - details.reviewDate.getYear()) * 12
				+ (currentDate.getMonth() - details.reviewDate.getMonth());

		if (months <= 24) {
			dateScore = 20;
		} else if (months > 24 && months <= 48) {
			dateScore = 15;
		} else if (months > 48 && months <= 72) {
			dateScore = 10;
		} else if (months > 72 && months <= 100) {
			dateScore = 5;
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
		ReviewRanking ranking = new ReviewRanking("");
		
		
		ArrayList<ArrayList<ReviewDetails>> reviews = ranking.retrieveReviews("");
		
		try {
			System.out.println("Positive Reviews are: ");
			ArrayList<ReviewDetails> detailsList = reviews.get(0);
			for (ReviewDetails reviewDetails : detailsList) {
				System.out.println();
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

	public ReviewDetails(String reviewText, int reviewRating, String date, int sentimentScore) {
		this.reviewText = reviewText;
		this.reviewRating = reviewRating;
		this.reviewScore = 0.0;
		this.sentimentScore = sentimentScore;
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
