package corealgorithm;

import java.io.*;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class AmazonMovieSentimentAnalysis {

	HashMap<String, ArrayList<String>> movieReviewMap;
	ArrayList<Integer> classLabel;
	ArrayList<String> reviewList;
	TextPreProcessing preProcessing;

	String trainingDocs[], testingDocs[];
	int trainingLabels[], testingLabels[], predictTestingLabel[];
	int correctCount = 0;
	int correctCountN = 0;

	int numClasses = 2;
	int[] classCounts; // number of docs per class
	String[] classStrings; // concatenated string for a given class
	int[] classTokenCounts; // total number of tokens per class
	HashMap<String, Double>[] condProb;
	HashSet<String> vocabulary; // entire vocabulary
	Map<String, Integer> termFrequency = new HashMap<>();
	int threshold;

	@SuppressWarnings("unchecked")
	public AmazonMovieSentimentAnalysis(String trainCSVFile) throws IOException {
		// Text Cleaning and Preparation
		long startTime = System.currentTimeMillis();
		classLabel = new ArrayList<>();
		reviewList = new ArrayList<>();
		classCounts = new int[numClasses];
		classStrings = new String[numClasses];
		classTokenCounts = new int[numClasses];
		condProb = new HashMap[numClasses];
		vocabulary = new HashSet<>();
		preProcessing = new TextPreProcessing();

		FileReader reader = new FileReader(trainCSVFile);
		int a = 0;

		for (CSVRecord csv : CSVFormat.EXCEL.parse(reader)) {
			// If its header continue
			if (a == 0) {
				a = 1;
				continue;
			}

			// Get all the proper cloumn values
			String reviewText = csv.get(5);
			String summary = csv.get(0);
			String words = csv.get(9);

			// Do all the preprocees in words
			reviewText = preProcessing.performTokenization(reviewText + " " + summary + " " + words);
			reviewText = preProcessing.doStem(reviewText);
//			preProcessing.termFreq(reviewText);

			reviewList.add(reviewText);
			classLabel.add(Integer.parseInt(csv.get(8)));
		}

		/*for (int i = 0; i < reviewList.size(); i++) {
			String text = reviewList.get(i);
			text = preProcessing.getTerms(text);
			reviewList.set(i, text);
		}*/

		int length = reviewList.size();
		trainingDocs = new String[length];
		trainingLabels = new int[length];

		for (int i = 0; i < length; i++) {

			trainingDocs[i] = reviewList.get(i);
			trainingLabels[i] = (int) classLabel.get(i);
		}

		for (int i = 0; i < numClasses; i++) {
			classStrings[i] = "";
			condProb[i] = new HashMap<>();
		}

		for (int i = 0; i < trainingLabels.length; i++) {
			classCounts[trainingLabels[i]]++;
			classStrings[trainingLabels[i]] += (trainingDocs[i] + " ");
		}

		for (int i = 0; i < numClasses; i++) {
			String[] tokens = classStrings[i].split(" ");
			classTokenCounts[i] = tokens.length;

			for (String token : tokens) {
				vocabulary.add(token);
				if (termFrequency.containsKey(token)) {
					termFrequency.put(token, termFrequency.get(token) + new Integer(1));
				} else {
					termFrequency.put(token, new Integer(1));
				}

				if (condProb[i].containsKey(token)) {
					double score = condProb[i].get(token);
					condProb[i].put(token, score + 1);
				} else {
					condProb[i].put(token, 1.0);
				}
			}
		}

		for (int i = 0; i < numClasses; i++) {
			Iterator<Map.Entry<String, Double>> iterator = condProb[i].entrySet().iterator();
			int vSize = vocabulary.size();

			while (iterator.hasNext()) {
				Map.Entry<String, Double> entry = iterator.next();
				String token = entry.getKey();
				Double score = entry.getValue();

				score = (score + 1) / (classTokenCounts[i] + vSize);
				condProb[i].put(token, score);
			}
		}

		long endTime = System.currentTimeMillis();
		System.out.println("Time taken to build this model: "
				+ (Math.round((endTime - startTime) * 100) / 100.0) / 60000 + " min. ");

	}

	private int classify(String reviewText) {

		int vSize = vocabulary.size();
		Double[] score = new Double[numClasses];

		for (int i = 0; i < score.length; i++) {
			score[i] = Math.log(classCounts[i] * 1.0 / trainingDocs.length);
		}

		String[] tokens = reviewText.split(" ");
		for (int i = 0; i < numClasses; i++) {
			for (String token : tokens) {
				if (condProb[i].containsKey(token)) {
					score[i] += Math.log(condProb[i].get(token));
				} else {
					score[i] += Math.log(1.0 / (classTokenCounts[i] + vSize));
				}
			}
		}

		double max = score[0];
		int predictLabel = 0;

		for (int i = 0; i < score.length; i++) {
			if (score[i] > max) {
				max = score[i];
				predictLabel = i;
			}
		}
		return predictLabel;
	}

	private void classifyAll(String testCSVFile) throws IOException {
		long startTime = System.currentTimeMillis();
		System.out.println("Vocabulary Size: " + vocabulary.size());
		preProcessing = new TextPreProcessing();
		ArrayList<Integer> testClassLabel = new ArrayList<>();
		ArrayList<String> testReviewList = new ArrayList<>();

		FileReader reader = new FileReader(testCSVFile);
		int a = 0;

		for (CSVRecord csv : CSVFormat.EXCEL.parse(reader)) {
			if (a == 0) {
				a = 1;
				continue;
			}

			String reviewText = csv.get(5);
			String summary = csv.get(0);

			reviewText = preProcessing.performTokenization(reviewText + " " + summary);
			reviewText = preProcessing.doStem(reviewText);
//			preProcessing.termFreq(reviewText);

			testReviewList.add(reviewText);
			testClassLabel.add(Integer.parseInt(csv.get(8)));
		}

		/*for (int i = 0; i < testReviewList.size(); i++) {
			String testText = testReviewList.get(i);
			testText = preProcessing.getTerms(testText);
			testReviewList.set(i, testText);
		}*/

		int length = testReviewList.size();
		testingDocs = new String[length];
		testingLabels = new int[length];
		predictTestingLabel = new int[length];

		for (int i = 0; i < length; i++) {
			if (classify(testReviewList.get(i)) == testClassLabel.get(i)) {
				correctCount++;
			}
		}

		System.out.println("Correctly classified " + correctCount + " out of " + length);
		System.out.println("Accuracy: " + (double) ((correctCount) * 1.0 / length * 1.0) * 100);

		long endTime = System.currentTimeMillis();
		System.out.println("Time taken to test the model: "
				+ (Math.round((endTime - startTime) * 100.0) / 100.0) / 60000 + " min");

	}

	public static void main(String[] args) throws IOException {

		String trainCSVFile = "Train50K.csv";
		String testCSVFile = "Test50K.csv";

		AmazonMovieSentimentAnalysis analysis = new AmazonMovieSentimentAnalysis(trainCSVFile);
		analysis.classifyAll(testCSVFile);
		System.out.println();
		System.out.println("Sample Single Reviews to check::");
		System.out.println("One of the best movie: " + analysis.classify("One of the best movie"));
		System.out.println("Movie is not worth compare to other movies: "
				+ analysis.classify("Movie is not worth compare to other movies"));
		System.out.println("This movie is generic and extremely bad: "
				+ analysis.classify("This movie is generic and extremely bad"));
		System.out.println("The movie has fantastic plot and amazing direction: "
				+ analysis.classify("The movie has fantastic plot and amazing direction"));
		System.out.println("Great Movie: " + analysis.classify("Great Movie"));
	}

}
