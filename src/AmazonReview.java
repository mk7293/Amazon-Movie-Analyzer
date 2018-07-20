public class AmazonReview {
	private String summary;
	private String reviewerName;
	private String reviewerID;
	private int rating;
	private String asin;
	private String helpFul;
	private String reviewText;
	private String reviewTime;
	private int sentiment;

	public AmazonReview(String summary, String reviewerName, String reviewerID, int rating, String asin, String helpFul, String reviewText, String reviewTime) {
		super();
		this.summary = summary;
		this.reviewerName = reviewerName;
		this.reviewerID = reviewerID;
		this.rating = rating;
		this.asin = asin;
		this.helpFul = helpFul;
		this.reviewText = reviewText;
		this.reviewTime = reviewTime;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getReviewerName() {
		return reviewerName;
	}
	public void setReviewerName(String reviewerName) {
		this.reviewerName = reviewerName;
	}
	public String getReviewerID() {
		return reviewerID;
	}
	public void setReviewerID(String reviewerID) {
		this.reviewerID = reviewerID;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public String getAsin() {
		return asin;
	}
	public void setAsin(String asin) {
		this.asin = asin;
	}
	public String getHelpFul() {
		return helpFul;
	}
	public void setHelpFul(String helpFul) {
		this.helpFul = helpFul;
	}
	public String getReviewText() {
		return reviewText;
	}
	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}
	public String getReviewTime() {
		return reviewTime;
	}
	public void setReviewTime(String reviewTime) {
		this.reviewTime = reviewTime;
	}public int getSentiment() {
		return sentiment;
	}
	public void setSentiment(int sentiment) {
		this.sentiment = sentiment;;
	}
	
	
}
