package mengg.classes.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


/**A class that automates the testing of the State identification and disambiguation approach. It computes precision, recall and F-measure.
 * The input data must be represented in a HashMap where the key is the paragraph id and the value is
 * true for "is state" and false otherwise. To print the report, simply create an object of this class, pass
 * the right arguments to the constructor and print the object.  */
public class Tester {
	private HashMap<String, Boolean> results; //The input HashMap which stores the results of the State identification algorithm
	private HashMap<String, Boolean> golds; //The HashMap that stores the results of human generated file (Gold Standard)
	private String goldFile;
	public static String getCurrentPath  = new java.io.File("").getAbsolutePath();
	public static void getResults(HashMap<String, Boolean> stateResults,String state){

		
		//HashMap<String, Boolean> stateResults = new HashMap<String, Boolean>();
		
			Tester tester = new Tester(stateResults,
					getCurrentPath + "/src/mengg/data/input/csvfolder/" + state + ".csv");
		
			System.out.println(tester);
		
	}
	
	/**Constructor: Assigns the argument results to the field results */
	public Tester(HashMap<String, Boolean> results, String goldFile){
		this.results = results;
		this.goldFile = goldFile;
	}

	
	/**Reads the gold file that contains the human generated results and returns a hashMap 
	 * representing this file where the key is the paragraph identifier and the value is boolean.  */
	public HashMap<String, Boolean> readFile(String csvFile){
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		BufferedReader br = null;
		String line= "";
		String csvSplitBy = ",";
		try{
			br = new BufferedReader(new FileReader(csvFile));
			while((line = br.readLine()) != null){
				String[] record = line.split(csvSplitBy);
				map.put(record[0],  Boolean.valueOf(record[1]));
			}
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		} finally {
			if(br != null){
				try{
					br.close();
				}catch (IOException e){
					e.printStackTrace();
				}
			}
		}
		return map;
	}
	/**Returns a list of those none state paragraphs that are miss-classified as 
	 * states by the state identification algorithm*/
	public ArrayList<String> getListOfFalsePositives(){
		ArrayList<String> list = new ArrayList<String>();
		for(Entry<String, Boolean> entry: golds.entrySet()){
			if(results.containsKey(entry.getKey())){
				if(!entry.getValue() && results.get(entry.getKey()))
				list.add(entry.getKey());
			}
		}
		return list;
	}
	
	/**Returns a list of those paragraphs that contain a correct sense of a state but miss-classified as
	 * none state by the state identification algorithm*/
	public ArrayList<String> getListOfFalseNegatives(){
		ArrayList<String> list = new ArrayList<String>();
		for(Entry<String, Boolean> entry: golds.entrySet()){
			if(results.containsKey(entry.getKey())){
				if(entry.getValue() && !results.get(entry.getKey()))
				list.add(entry.getKey());
			}
		}
		return list;
	}
	
	/**Returns a list of those paragraphs that contain a correct sense of a state and correctly classified as 
	 * correct sense by the state identification algorithm */
	public ArrayList<String> getListOfTruePositives(){
		ArrayList<String> list = new ArrayList<String>();
		for(Entry<String, Boolean> entry: golds.entrySet()){
			if(results.containsKey(entry.getKey())){
				if(entry.getValue() && results.get(entry.getKey()))
				list.add(entry.getKey());
			}
		}
		return list;
	}
	
	/**Returns a list of those paragraphs that contain none state senses and correctly classified as
	 * none states by the state identification algorithm */
	public ArrayList<String> getListOfTrueNegatives(){
		ArrayList<String> list = new ArrayList<String>();
		for(Entry<String, Boolean> entry: golds.entrySet()){
			if(results.containsKey(entry.getKey())){
				if(!entry.getValue() && !results.get(entry.getKey()))
				list.add(entry.getKey());
			}
		}
		return list;
	}
	
	/**Returns the value of precision which is a measure of cleanliness */
	public double computePrecision(ArrayList<String> listOfTP,ArrayList<String> listOfFP){
		double p = 0.0;
		if ((listOfTP.size() + listOfFP.size()) != 0)
		p = (double) (listOfTP.size())
				/(double) (listOfTP.size() + listOfFP.size());
		return p;
	}
	
	/**Returns the value of recall which is a measure of completeness */
	public double computeRecall(ArrayList<String> listOfTP, ArrayList<String> listOfFN){
		double r = 0.0;
		if((listOfTP.size() + listOfFN.size()) != 0)
		r = (double) (listOfTP.size())
				/(double) (listOfTP.size() + listOfFN.size());
		return r;
	}
	
	/**Returns the value of F-measure which combines both precision and recall */
	public double computeFMeasure(ArrayList<String> listOfTP, ArrayList<String> listOfFP, 
			ArrayList<String> listOfFN){
		double f = 0.0;
		double p = computePrecision(listOfTP, listOfFP);
		double r = computeRecall(listOfTP, listOfFN);
		if ((p + r) != 0)
		f = (double) 2*p*r/ (double) (p + r);
		return f;
	}
	
	/**Returns the value of accuracy */
	public double computeAccuracy(ArrayList<String> listOfTP, ArrayList<String> listOfTN, ArrayList<String>
	listOfFP, ArrayList<String> listOfFN){
		double a = 0.0;
		double denominator = listOfTN.size() + listOfTP.size() + listOfFN.size() + listOfFP.size(); 
		if (denominator != 0) a = (double) (listOfTN.size() + listOfTP.size())/(double) denominator; 
		return a;
	}
	
	public @Override String toString(){
	golds = readFile(goldFile);
    /*for(String s:golds.keySet()){
        System.out.println(s + "=" + golds.get(s));
    }*/
	ArrayList<String> listOfTP = getListOfTruePositives();
	ArrayList<String> listOfFN = getListOfFalseNegatives();
	ArrayList<String> listOfTN = getListOfTrueNegatives();
	ArrayList<String> listOfFP = getListOfFalsePositives();
	String output = "Report:";
	output += "\nPrecision: " + computePrecision(listOfTP, listOfFP);
	output += "\nRecall: " + computeRecall(listOfTP, listOfFN);
	output += "\nF-Measure: " + computeFMeasure(listOfTP, listOfFP, listOfFN);
	output += "\nAccuracy: " + computeAccuracy(listOfTP, listOfTN, listOfFP, listOfFN);
	output += "\n----------------------------------------";
	if(listOfTP.size() != 0){
		output += "\nList of correct definitions:";
		for (String s: listOfTP){
			output += "\n" + s;
		}
	} else output += "\nThere are no correctly identified definitions";
	
	output += "\n----------------------------------------";
	
	if(listOfFN.size() != 0){
		output += "\nList of missing definitions:";
		for(String s: listOfFN){
			output += "\n" + s;
		}
	} else output += "\nThere are no missing definitions";
	
	output += "\n----------------------------------------";
	
	if(listOfFP.size() != 0){
		output += "\nList of incorrect definitions";
		for(String s: listOfFP){
			output += "\n" + s;
		}
	} else output += "\nThere are no incorrectly identified definitions";
	
	output += "\n----------------------------------------";
	
	if(listOfTN.size() != 0){
		output += "\nList of paragraphs correctly identified as none definitions:";
		for(String s: listOfTN){
			output += "\n" + s;
		}
	}
	return output;
	}
}
