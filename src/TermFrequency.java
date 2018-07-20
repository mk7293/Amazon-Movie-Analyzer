import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class TermFrequency {
	static final String fileNameDefined = "analyzed_data/Movies_and_TV_Training_Dataset.csv";
	static final String fileWithSentiment = "analyzed_data/Movies_and_TV_Training_Dataset_With_Sentiment.csv";

	String[] stopWordsList;
	ArrayList<String> amazonReviews;
	Map<String, Integer> termMap = new HashMap<>();
	ArrayList<AmazonReview> reviewobjs;
	
	private void readCSVFile(String fileName){
		amazonReviews = new ArrayList<String>();
		reviewobjs = new ArrayList<AmazonReview>();
		Reader in;
		try {
			in = new FileReader(fileName);
			Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
		    for (CSVRecord record : records) {
		    	if (record.get(0).equalsIgnoreCase("summary"))
		    		continue;
		    	AmazonReview obj = new AmazonReview(record.get(0),record.get(1),record.get(2),Integer.parseInt(record.get(3)),record.get(4),
		    			record.get(6),record.get(7),record.get(8));
			    reviewobjs.add(obj);
		    	amazonReviews.add(record.get(7));
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<String> getWords(String fileName){
		ArrayList<String> wordList = new ArrayList<String>();
		try{
			BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
			String strLine;
			while ((strLine = reader.readLine()) != null) {
				wordList.add(strLine);
			}
			Collections.sort(wordList);
		}catch(Exception e){
			e.printStackTrace();;
		}
		return wordList;
	}
	
	private void getFileContent(String fileName) {
		System.out.println("A");
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
		System.out.println("B");
		ArrayList<String> bkAmazonReviews = amazonReviews;
		amazonReviews = new ArrayList<>();

		for (String string : bkAmazonReviews) {
			String[] tokens = string.split("[ .,?!:;$%&*+'\"#\\()\\-\\^]+");
			String tokenizedString = "";
			for (String token : tokens) {
				tokenizedString += token + " ";
			}
			amazonReviews.add(tokenizedString.trim());
		}
	}

	private void getStopWords(String filename) {
		System.out.println("C");
		String stopWordFileName = filename;
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
		System.out.println("D");
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
		System.out.println("E");
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
		System.out.println("F");
		for (String string : amazonReviews) {
			String[] terms = string.split(" ");
			for (String term : terms) {
				Stemmer st = new Stemmer();
				st.add(term.toCharArray(),term.length());
				st.stem();
				term = st.toString();
				if (!termMap.containsKey(term)) {
					termMap.put(term, new Integer(1));
				} else {
					termMap.put(term, new Integer(termMap.get(term) + 1));
				}
			}
		}
	}

	private void writeText(Map<String, Integer> tMap, String outputFileName) {
		System.out.println("G");
		try {
			PrintWriter writer = new PrintWriter(outputFileName, "UTF-8");
			String mm = new String();
			for (Map.Entry<String, Integer> entry : tMap.entrySet()) {
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
	
	private void writeCsv(Map<String, Integer> tMap, String fileName){
		System.out.println("H");
		try{
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Term","Frequencies"));
            for (Map.Entry<String, Integer> entry : tMap.entrySet()) {
				csvPrinter.printRecord(entry.getKey(), entry.getValue());
			}
            csvPrinter.flush();
            csvPrinter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void performMatching(){
		NLP.init();
		try{
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileWithSentiment));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("summary","reviewerName","reviewerID","Rating","MovieID","helpful","reviewText","reviewTime","Sentiment"));
        for (int i=0; i<reviewobjs.size();i++) {
			csvPrinter.printRecord(reviewobjs.get(i).getSummary(),reviewobjs.get(i).getReviewerName(),reviewobjs.get(i).getReviewerID(),
					reviewobjs.get(i).getRating(),reviewobjs.get(i).getAsin(),reviewobjs.get(i).getHelpFul(),reviewobjs.get(i).getReviewText(),reviewobjs.get(i).getReviewTime(),NLP.findSentiment(reviewobjs.get(i).getReviewText()));
        }
        csvPrinter.flush();
        csvPrinter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {	
		TermFrequency frequency = new TermFrequency();		
		/*File dirFile = new File(fileNameDefined);
		File[] files = dirFile.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				String eachFilePath = fileNameDefined + "\\" + file.getName();
				frequency.getFileContent(fileNameDefined);}}*/
		frequency.getStopWords("stopwords.txt");
		frequency.readCSVFile(fileNameDefined);
		frequency.performTokenization();
		frequency.removeStopWord();
		frequency.indexingTerms();
		frequency.writeText(frequency.termMap,"stemmed-term-frequencies.txt");
		frequency.writeCsv(frequency.termMap,"stemmed-Tfrequencies.csv");
		frequency.performMatching();
		
	}

}
