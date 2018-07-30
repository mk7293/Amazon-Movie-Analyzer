package corealgorithm;

import java.io.*;
import java.util.*;

public class TextPreProcessing {

	public static final String CURR_PATH = System.getProperty("user.dir");

	public String performTokenization(String text) {
		String[] tokens = text.split("\\W+");
		String tokenizedText = "";
		for (String token : tokens) {
			tokenizedText += token.toLowerCase() + " ";
		}
		return tokenizedText;
	}

	private ArrayList<String> getStopWords() {
		String stopWordPath = CURR_PATH + "//" + "stopwords.txt";
		ArrayList<String> list = new ArrayList<>();
		try {
			// Reading the document over the buffered reader
			BufferedReader bufferedReader = new BufferedReader(new FileReader(stopWordPath));

			String stopWordContent;
			while ((stopWordContent = bufferedReader.readLine()) != null) {
				list.add(stopWordContent);
			}
			bufferedReader.close();
		} catch (IOException e) {

		}

		Collections.sort(list);
		return list;

	}
	
	private int searchStopWord(String term, ArrayList<String> stopWordList) {
		int lo = 0, hi = stopWordList.size() - 1;

		while (lo <= hi) {
			int mid = lo + (hi - lo) / 2;
			int result = term.compareTo(stopWordList.get(mid));

			if (result < 0)
				hi = mid - 1;
			else if (result > 0)
				lo = mid + 1;
			else
				return mid;
		}
		return -1;
	}

	public String removeStopWords(String text) {
		ArrayList<String> stopWordList = getStopWords();
		String[] terms = text.split(" ");
		String newText = "";
		for (String term : terms) {
			if (searchStopWord(term, stopWordList) == -1) {
				newText += term + " ";
			}
		}
		return newText;
	}
	
	public String doStem(String text) {
		String[] terms = text.split(" ");
		String newTerms = "";
		for (String term : terms) {
			Stemmer stemmer = new Stemmer();
			stemmer.add(term.toCharArray(), term.length());
			stemmer.stem();
			if (stemmer.toString().length() == 1) {
				continue;
			}
			newTerms += stemmer.toString() + " ";
		}
		return newTerms;
	}

}
