import java.io.*;
import java.util.*;

public class TermFrequency {

	String[] stopWordsList;
	ArrayList<String> amazonReviews;
	Map<String, Integer> termMap;

	private void getFileContent(String fileName) {
		amazonReviews = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
			String strLine;

			while ((strLine = reader.readLine()) != null) {
				String strs[] = strLine.split(",");
				if (strs[0].equalsIgnoreCase("summary"))
					continue;
				amazonReviews.add(strs[0].toLowerCase());
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void performTokenization() {
		ArrayList<String> bkAmazonReviews = amazonReviews;
		amazonReviews = new ArrayList<>();

		for (String string : bkAmazonReviews) {
			String[] tokens = string.split("\\W+");
			String tokenizedString = "";
			for (String token : tokens) {
				tokenizedString += token + " ";
			}
			amazonReviews.add(tokenizedString.trim());
		}

	}

	private void getStopWords() {
		String stopWordFileName = "D:\\KPT\\stopwords.txt";
		try {

			BufferedReader bufferedReader = new BufferedReader(new FileReader(stopWordFileName));
			int totalStopWordCount = 0, count = 0;
			String stopWordContent;

			while (bufferedReader.readLine() != null) {
				totalStopWordCount++;
			}
			bufferedReader.close();

			stopWordsList = new String[totalStopWordCount];
			bufferedReader = new BufferedReader(new FileReader(stopWordFileName));

			while ((stopWordContent = bufferedReader.readLine()) != null) {
				stopWordsList[count] = stopWordContent;
				count++;
			}

			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Arrays.sort(stopWordsList);
	}

	private int searchStopWord(String term) {
		int lo = 0, hi = stopWordsList.length - 1;

		while (lo <= hi) {
			int mid = lo + (hi - lo) / 2;
			int result = term.compareTo(stopWordsList[mid]);

			if (result < 0)
				hi = mid - 1;
			else if (result > 0)
				lo = mid + 1;
			else
				return mid;
		}
		return -1;
	}

	private void removeStopWord() {
		ArrayList<String> bkAmazonReviews = amazonReviews;
		amazonReviews = new ArrayList<>();

		for (String string : bkAmazonReviews) {
			String[] terms = string.split(" ");
			String removedTerms = "";
			for (String term : terms) {
				if (searchStopWord(term) == -1) {
					removedTerms += term + " ";
				}
			}
			amazonReviews.add(removedTerms);
		}
	}

	private void indexingTerms() {
		termMap = new HashMap<>();
		for (String string : amazonReviews) {
			String[] terms = string.split(" ");
			for (String term : terms) {
				if (!termMap.containsKey(term)) {
					termMap.put(term, new Integer(1));
				} else {
					termMap.put(term, new Integer(termMap.get(term) + 1));
				}
			}
		}
	}

	private void writeText(int count) {
		try {
			PrintWriter writer = new PrintWriter("term-frequency-" + count + ".txt", "UTF-8");
			String mm = new String();
			for (Map.Entry<String, Integer> entry : termMap.entrySet()) {
				mm += String.format("%-15s", entry.getKey());
				mm += entry.getValue();
				mm += "\n";
			}
			writer.println(mm);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		TermFrequency frequency = new TermFrequency();
		frequency.getStopWords();

		String fileNameDefined = "D:\\KPT\\Movies_and_TV.JSON";
		File dirFile = new File(fileNameDefined);
		File[] files = dirFile.listFiles();
		int count = 0;
		for (File file : files) {
			if (file.isFile()) {
				String eachFilePath = fileNameDefined + "\\" + file.getName();
				frequency.getFileContent(eachFilePath);
				frequency.performTokenization();
				frequency.removeStopWord();
				frequency.indexingTerms();
				frequency.writeText(count++);
			}
		}
	}

}
