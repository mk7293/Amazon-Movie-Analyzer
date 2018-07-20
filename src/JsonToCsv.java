import java.io.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import org.apache.commons.io.FileUtils;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class JsonToCsv {

	public static void main(String[] args) {
		File folder = new File("json");
		File[] listOfFiles = folder.listFiles();
		for(int i=0;i<listOfFiles.length;i++)
		{
			convertJsonTOCsv("json\\"+listOfFiles[i].getName());
		}
	
	}
	static void convertJsonTOCsv(String filename){
		File file=new File(filename+".csv");
		try {
			String text = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
			JSONObject output =  new JSONObject(text);
			JSONArray docs = output.getJSONArray("data");
            String csv = CDL.toString(docs);
            FileUtils.writeStringToFile(file, csv,"UTF-8",true);
            System.out.println("DONE\t"+filename);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }        
	}
}
