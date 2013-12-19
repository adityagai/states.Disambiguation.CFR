package mengg.classes.main;

import mengg.classes.helper.*;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: prasannjit
 * Date: 11/23/13
 * Time: 9:37 PM
 * To change this template use File | Settings | File Templates.
 */
/**
 *@author Prasannjit
* This classifies the paragraphs for an "unseen" input into positive and negative
* Used to rank the document collection given the 
* list of positive and negative attributes set for the input state
* This is then compared to the gold-standard csv files and results are printed
* */
public class RankInputImpl {
    public static String getCurrentPath = new java.io.File("").getAbsolutePath();
    public static String directoryPath = getCurrentPath + "/src/mengg/data/input/states/";

    public static void main(String[] args) throws IOException {
        System.out.print("Enter the state to test for : ");
        
        //get the "unseen" state input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String statename = br.readLine();
        
        //create the training data for the input "state"
        TrainingDataInput.trainingDataInput(statename);
        
        //get the document token map
        HashMap<String, ArrayList<NewToken>> documentTokenMap =
                CreateDocumentTokenMap.createDocumentTokenMap(statename,directoryPath);

        //create section documents and initialize p-scores and n-scores
        ArrayList<SectionDocument> sectionDocuments = new ArrayList<SectionDocument>();

        //make section documents and initialize p-scores and n-scores for the section documents 
        addDocuments(sectionDocuments,documentTokenMap);
        
        String rankedPath = getCurrentPath + "/src/mengg/data/input" + "/" + "ranked";
        File rankedDir = new File(rankedPath);
        if(!rankedDir.exists()){
        	rankedDir.mkdir();
        }

        String rankedStatePath = rankedPath + "/" + statename;

        File rankedState = new File(rankedStatePath);
        if(!rankedState.exists()){
            if(rankedState.mkdir()){
                System.out.println("created the folder for " + statename);
            }
        }

        String postiveTrainingPath = rankedStatePath + "/pos";
        String negativeTrainingPath = rankedStatePath + "/neg";

        ArrayList<NewToken> positiveAttributesList = new ArrayList<NewToken>();
        ArrayList<NewToken> negativeAttributesList = new ArrayList<NewToken>();

        getSeedAttributes(positiveAttributesList, negativeAttributesList, statename);

        updateScores(sectionDocuments,positiveAttributesList,negativeAttributesList);


            //list of best positive and negative documents
            ArrayList<String> PositiveList  = new ArrayList<String>();
            ArrayList<String> NegativeList = new ArrayList<String>();
            addToList(sectionDocuments,PositiveList,NegativeList);

            //first create set for positive and negative results
            createTrainingSet(PositiveList,postiveTrainingPath,statename);//,positiveRankedAttributes);
            createTrainingSet(NegativeList,negativeTrainingPath,statename);//, negativeRankedAttributes);

            HashMap<String,Boolean> paragraphClassifier = new HashMap<String, Boolean>();

            //update Map for positive results
            addtoMapFileNames(paragraphClassifier,PositiveList,true);
            addtoMapFileNames(paragraphClassifier,NegativeList,false);
            System.out.println("for the state=>" + statename);
            
            //check if the file exists in the CSV folder, if yes, then call the Tester
            String csvFolderPath = getCurrentPath + "/src/mengg/data/input/csvfolder";///" + statename + ".csv";
            File csvFolder = new File(csvFolderPath);
            boolean matches = false;
            if(csvFolder.exists()){
            	for(String s:csvFolder.list()){
            		if(statename.compareToIgnoreCase(s)==0){
            			matches=true;
            			Tester.getResults(paragraphClassifier, statename);
            		}
            	}
            }
            if(!matches){
            	System.out.println("CSV File for " + statename + " doesn't exist, please add the csv file for gold results");
            	for(String para: paragraphClassifier.keySet()){
            		System.out.println(para + "," + paragraphClassifier.get(para));	
            	}
            }
            

        System.out.println();
    }

    /**
     * Labels the paragraphs as "Positive" and "Negative"
     * @param paragraphClassifier map to be updated
     * @param list of paragraphs
     * @param value 
     */
    private static void addtoMapFileNames(HashMap<String, Boolean> paragraphClassifier,
                                          ArrayList<String> list, boolean value) {

        for(String paraName : list){
            paragraphClassifier.put(paraName,value);
        }
    }

    /**
     * method to obtain the seed attributes from the seed collection
     * of the given state, uses the populate method to populate
     * the seeds and then processes the individual terms or attributes
     * @param positiveAttributesList stores the positive attributes
     * @param negativeAttributesList stores the negative attributes
     * @param statename the input state
     * */
    private static void getSeedAttributes(ArrayList<NewToken> positiveAttributesList, 
    						ArrayList<NewToken> negativeAttributesList, 
    						String statename) {
       
    	String positiveSeedPath = getCurrentPath + "/src/mengg/data/input/positive.txt";


        String negativeSeedPath = getCurrentPath + "/src/mengg/data/input/negative.txt";

        populateSeedAttributes(positiveSeedPath, positiveAttributesList, statename);
        populateSeedAttributes(negativeSeedPath, negativeAttributesList, statename);

    }

    /**
     * method implementation to get the attributes from the seed file
     * uses an array-list of tokens to get the positive and the negative
     * attributes
     * @param seedPath path to the seed file
     * @param attributesList list of tokens to be stored
     * @param state input "state"
     * */
    private static void populateSeedAttributes(String seedPath,
                                               ArrayList<NewToken> attributesList, String state) {

        //check if the file exists or not
        if(seedPath.isEmpty()){
            System.err.println("empty path provided....exiting");
            System.exit(1);
        }

        File file = new File(seedPath);

        if(!file.exists()){
            System.err.println("could not locate the file");
            System.exit(1) ;
        }

        if(!file.canRead()){
            System.err.println("cannot read this file");
            System.exit(1);
        }

        //get the terms from the file as attribute tokens and add it to the list
        ArrayList<String> wordList = new ArrayList<String>();
        getAttributesImpl(file,attributesList,wordList,state);
    }

    /**
     * Implementation method to get the attributes from the seed file
     * and adding it to the attribute list
     * @param file to be read
     * @param attributesList list of tokens to be stored
     * @param wordList tracks the word read already
     * @param state input "state"
     * */
    private static void getAttributesImpl(File file, 
    		ArrayList<NewToken> attributesList, 
    		ArrayList<String> wordList, 
    		String state) {
        {

            FileReader reader = null;
            if(file.canRead()){
                try{
                    reader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line;
                    String line1 = "";
                    while((line=bufferedReader.readLine())!=null){
                        line1=line1 + line + " ";
                    }
                    line1=line1.trim();
                    line1=InputHandler.specialWordHandler(line1); //handle the special words
                    line1 = InputHandler.filterString(line1); //handle the special characters and stop-words
                    StringTokenizer tokenizer = new StringTokenizer(line1);

                    while(tokenizer.hasMoreTokens()){
                        String ss= tokenizer.nextToken();

                        if(ss.compareTo(state)==0 && tokenizer.hasMoreTokens()){
                            ss=tokenizer.nextToken();
                        }
                        //if the wordList doesn't contain the word then create a new token for it
                        NewToken token = new NewToken();
                        token.word=ss;

                        boolean match=false;
                        if(wordList.contains(token.word.toLowerCase())){
                            //get the token from the attribute list and increase its count

                            for(NewToken temp: attributesList){
                                if((token.word.compareToIgnoreCase(temp.word)==0)){
                                    match=true;
                                    temp.count++;
                                    break;
                                }
                            }
                        }
                        if(!match){
                            //create a new token with this, add to the word list and to the
                            //attribute list
                            token.count=1;
                            wordList.add(token.word.toLowerCase());
                            attributesList.add(token);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();  
                } catch (IOException e) {
                    e.printStackTrace();  
                }
            }
            else {
            	System.err.println("cannot read this file");
            }

        }

    }

    /**
     * creates the list of positive and negative sense documents from the section documents
     * based on positive and negative scores
     * @param sectionDocuments list of paragraphs
     * @param positiveList files for positive paragraphs
     * @param negativeList files for negative paragraphs
     */
    private static void addToList(ArrayList<SectionDocument> sectionDocuments,
                                  ArrayList<String> positiveList,
                                  ArrayList<String> negativeList) {

        for(SectionDocument section: sectionDocuments){
            if(section.pscore>section.nscore){
                positiveList.add(section.sectionName);
            }
            else{
                negativeList.add(section.sectionName);
            }

        }
    }


    /**
    * Method to create the Training Sets "Positive" and "Negative"
    * to be used by the WEKA classifier
    * @param sectionNames name of the documents 
    * @param path document path
    * @param state to be ranked
    * */
    private static void createTrainingSet(ArrayList<String> sectionNames,
                                          String path, String state) {


        File pos = new File(path);
        if(!pos.exists()){

            //if folder doesn't exist then create it
            boolean result = pos.mkdir();
            if(result){
                // System.out.println("folder " + " created");
            }
        }
        FileReader fileReader;
        for(String section: sectionNames){
            try {
                String getPath = getCurrentPath + "/src/mengg/data/input/states/"
                        + state + "/" + section + ".txt";
                fileReader = new FileReader(new File(getPath));
                BufferedReader reader = new BufferedReader(fileReader);
                String line=null;

                String topDocPath = path + "/" + section + ".txt";
                File topFile = new File(topDocPath);
                FileWriter fileWriter = null;

                if(topFile.createNewFile()){
                    //System.out.println("created file: " + topFile.getName());
                }
                if(topFile.exists()){
                    fileWriter = new FileWriter(topFile);
                    while((line=reader.readLine())!=null){
                        fileWriter.append(line);
                    }
                    if(fileWriter!=null){
                        fileWriter.close();
                    }
                }
                if(reader!=null){
                    reader.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }



    /**
    * method to update the document scores based on the seed attributes
    * @param sectionDocuments list of section Documents
    * @param positiveAttributesList list of positive attributes
    * @param negativeAttributesList list of negative attributes
    * */
    private static void updateScores(ArrayList<SectionDocument> sectionDocuments,
                                     ArrayList<NewToken> positiveAttributesList,
                                     ArrayList<NewToken> negativeAttributesList) {

        for(SectionDocument section: sectionDocuments){
            //get the individual tokens from the section one by one
            for(NewToken token: section.termList){
                section.pscore += rankToken(token,positiveAttributesList);
                section.nscore += rankToken(token,negativeAttributesList);
            }
        }
    }

    /**
     * method to score the documents on the basis of seed attributes, matches each term with the attributes list
     * if match then return 1
     * @param term the term in the section document which is to be ranked
     * @param attributesList list of attribute tokens
     * @return double score for this term
     * */
    private static double rankToken(NewToken term,
                                    ArrayList<NewToken> attributesList) {
        double score = 0.0;
        for(NewToken token : attributesList){
            if(term.word.compareToIgnoreCase(token.word)==0){
                score=1.0;
            }
        }//System.out.println(score);
        return score;

    }


    /**
    * method to create a list of document for the sections having p-score and n-score attributes
    * and a list containing all the terms in the document, here individual paragraphs
    * @param sectionDocuments 
    * @param documentTokenMap
    * 
    * */
    private static void addDocuments(ArrayList<SectionDocument> sectionDocuments,
                                     HashMap<String, ArrayList<NewToken>> documentTokenMap) {

        for(String s : documentTokenMap.keySet()){
            SectionDocument section = new SectionDocument();
            section.termList = documentTokenMap.get(s);
            section.sectionName=s;
            section.nscore=0.0;
            section.pscore=0.0;
            sectionDocuments.add(section);
        }
    }
}
