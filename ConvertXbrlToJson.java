package com.xbrltojson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class ConvertXbrlToJson {

	//	Reading XBRL file from input location
	public static void main(final String[] args) throws FileNotFoundException, IOException {
		//Input file location to be changed before using the conversion tool. it is based on current environment setup.
		final String fileName = "C:/Users/muthubal/eclipse-workspace/XBRLtoJSONConv/input/20230624/PostmanFileDuplicate.txt";
		String fileStr = readFileAsString(fileName);
//		fileStr = fileStr.substring(1);
		loadXbrlFromFile(fileStr);
	}

	//	Conversion XBLR to JSON static method
	public static String loadXbrlFromFile(final String fileStr) throws FileNotFoundException, IOException {
		
		//XBRL mapping fields
		String JSONFields[] = { "mci-sas:Revenue", "mci-isa:NameOfAuditFirm","mci-sas:NameOfAuditFirm",
				"mci-sas:ProfitLoss","mci-sas:IncomeFromMainOperations","mci-sas:FinanceExpenses",
				"mci-sas:OperatingProfitLoss","mci-sas:FinanceCosts","mci-sas:CurrentAssetsAbstract",
				"mci-sas:CurrentLiabilities", "mci-sas:Assets", "mci-sas:Liabilities", "mci-sas:Equity",
				"mci-sas:NonCurrentLiabilities","mci-sas:DepreciationAndAmortisationExpense"};
		
		//	Getting User Directory location
		String directory=System.getProperty("user.dir");
		
		//	Getting date to append to the output file
		String dateTime = new SimpleDateFormat("yyyyMMddHHmm'.json'").format(new Date());
		String fileLocation=directory+File.separator+"ConvertXbrlToJson_"+dateTime;

		String jsonPrettyPrintString = null;
		try {
			JSONObject jsonObjectAll = new JSONObject(fileStr.substring(1));
			JSONObject jsonObjectRoot = (JSONObject) jsonObjectAll.get("SimahRs");
			JSONObject jsonObjectBody = (JSONObject) jsonObjectRoot.get("Body");
			String contentStr = jsonObjectBody.getString("data");
			Path destpath = Paths.get(fileLocation.replace(fileLocation.split("\\.")[1], "json"));
			int PRETTY_PRINT_INDENT_FACTOR = 4;

			JSONObject xmlJSONObj = XML.toJSONObject(contentStr);
			JSONObject jsonBody = (JSONObject) xmlJSONObj.get("xbrli:xbrl");

			JSONObject jsonObject = new JSONObject();
			String JSONConvString = "";
				for (String JSONField : JSONFields) {
					JSONConvString = JSONField.replace("-", "_");	//Replacing "-" to "_"
					JSONConvString = JSONConvString.replace(":", "_");	//Replacing ":" to "_"
					if(jsonBody.has(JSONField)) {
						jsonObject.put(JSONConvString, jsonBody.get(JSONField));
						}else {
							// Fields to be skipped based on type
							if (JSONField != "mci-sas:NameOfAuditFirm" & JSONField != "mci-sas:OperatingProfitLoss" 
									& JSONField !="mci-sas:FinanceCosts" & JSONField != "mci-isa:NameOfAuditFirm" &
									JSONField != "mci-sas:IncomeFromMainOperations" & JSONField !="mci-sas:FinanceExpenses") {								
								jsonObject.put(JSONConvString, new JSONArray());
							}
						}
				}
			jsonPrettyPrintString = jsonObject.toString(PRETTY_PRINT_INDENT_FACTOR);

			if (isJSONValid(jsonPrettyPrintString)) {
				try {
					byte[] arr = jsonPrettyPrintString.getBytes();
					Files.write(destpath, arr);
					System.out.println("JSON File created at " + fileLocation.replace(fileLocation.split("\\.")[1], "json"));
				} catch (IOException ex) {
					System.out.print("Invalid Path");
				}
			}

		} catch (JSONException je) {
			System.out.println(je.toString());
		}

		return jsonPrettyPrintString;
	}

	public static String readFileAsString(final String fileName) {
		String content = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			String ls = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			// delete the last new line separator
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			reader.close();

			content = stringBuilder.toString();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return content;
	}

	public static boolean isJSONValid(final String test) {
		if (test != null) {
			try {
				new JSONObject(test);
			} catch (JSONException ex) {
				try {
					new JSONArray(test);
				} catch (JSONException ex1) {
					ex1.printStackTrace();
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
