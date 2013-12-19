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
* @author Prasannjit
* Used to rank the document collection given the state term.
* Gets the seed terms to form the initial word cluster, then adds terms to each of the 
* positive and negative clusters.
* The clusters are then used to score the paragraphs as positive and negative relative to the
* "state" and the "non-state" sense.
* If the paragraph collection size is below 20 all the paragraphs are chosen to be
* in the ranked folder.
* Currently 10 iterations are being used to obtain the final cluster
* */
public class RankParagraphsImpl {

    private static double denominatorBestAttributes=10;
    private static Double scoreThreshold = 0.005;
    private static int bestResultCount = 20;
    private static int totalIterations = 10;
    public static String getCurrentPath = new java.io.File("").getAbsolutePath();
    public static String directoryPath = getCurrentPath + "/src/mengg/data" + "/" + "states";

    public static void rankDocumentForState(String statename,
                                            HashMap<String,Double> positiveFeatures,
                                            HashMap<String,Double> negativeFeatures){

        //get the document token map
        HashMap<String, ArrayList<NewToken>> documentTokenMap =
                CreateDocumentTokenMap.createDocumentTokenMap(statename,directoryPath);

        ArrayList<SectionDocument> sectionDocuments = new ArrayList<SectionDocument>();

        ArrayList<NewToken> positiveAttributesList = new ArrayList<NewToken>();
        ArrayList<NewToken> negativeAttributesList = new ArrayList<NewToken>();

        HashMap<String, Double> positiveAttributesMap = new HashMap<String, Double>();
        HashMap<String, Double> negativeAttributesMap = new HashMap<String, Double>();

        HashMap<String, Double> relativeScorePositive = new HashMap<String, Double>();
        HashMap<String, Double> relativeScoreNegative = new HashMap<String, Double>();

        //make section documents and initialize p-scores and n-scores for the section documents 
        addDocuments(sectionDocuments,documentTokenMap);

        //get the seed attributes from the seed collection
        getSeedAttributes(positiveAttributesList, negativeAttributesList, statename);

        //get the positive and negative attributes from the seeds sorted and processed
        //add to the maps, remove the terms appearing in both the lists
        addToAttributesMap(positiveAttributesMap,positiveAttributesList,negativeAttributesList);
        addToAttributesMap(negativeAttributesMap,negativeAttributesList,positiveAttributesList);
        
        //obtain the list of attributes from the seed set, forms the initial clusters
        positiveAttributesList=refineMap(positiveAttributesMap);
        negativeAttributesList=refineMap(negativeAttributesMap);

        ArrayList<String> finalPositiveDocuments  = new ArrayList<String>();
        ArrayList<String> finalNegativeDocuments = new ArrayList<String>();

        boolean moreIterations = true;
        int numberIterations = 0;
        
        while(moreIterations && numberIterations<totalIterations){

        	//update the score of the sectionDocuments based on initial cluster
            updateScores(sectionDocuments,positiveAttributesList,negativeAttributesList);

            HashMap<String, Double> negativeDocumentsMap = new HashMap<String, Double>();
            HashMap<String, Double> positiveDocumentsMap = new HashMap<String, Double>();
            
            //list of best positive and negative documents
            ArrayList<String> bestPositiveDocuments  = new ArrayList<String>();
            ArrayList<String> bestNegativeDocuments = new ArrayList<String>();

            //create positive and negative document representations of the section document
            addToSectionMap(sectionDocuments,positiveDocumentsMap,negativeDocumentsMap);

            //get the best documents based on their scores
            getBestDocuments(positiveDocumentsMap, bestPositiveDocuments);
            getBestDocuments(negativeDocumentsMap, bestNegativeDocuments);

            //get the terms from the positive and the negative documents
            //one by one and see their scores in the positive and negative
            //attribute list
            //list of tokens from the ranked documents
            ArrayList<NewToken> positiveRankedAttributes = new ArrayList<NewToken>();
            ArrayList<NewToken> negativeRankedAttributes = new ArrayList<NewToken>();

            //get token lists from the best positive and the best negative documents
            //using the document token map created above
            getTokensFromBestDocuments(positiveRankedAttributes,documentTokenMap,bestPositiveDocuments);
            getTokensFromBestDocuments(negativeRankedAttributes,documentTokenMap,bestNegativeDocuments);

            //sort the attributes by their counts
            sort(positiveRankedAttributes);
            sort(negativeRankedAttributes);

            //if "state" related references appear in the negative cluster, ignore it
            for(NewToken token: negativeRankedAttributes){
                if(token.word.compareToIgnoreCase("state")==0 || token.word
                        .compareToIgnoreCase("states")==0){
                    token.count=0;
                }
            }

            //filter the terms having more importance in the opposite cluster
            positiveRankedAttributes = processAttributes(negativeAttributesMap,positiveRankedAttributes);
            negativeRankedAttributes = processAttributes(positiveAttributesMap,negativeRankedAttributes);

            //update the attributes Map with the new terms
            updateAttributesMap(positiveAttributesMap,positiveRankedAttributes);
            updateAttributesMap(negativeAttributesMap,negativeRankedAttributes);

            //create a list representation of the cluster map, get the cluster as a list
            positiveAttributesList=refineMap(positiveAttributesMap);
            negativeAttributesList=refineMap(negativeAttributesMap);

           //get the relative score of the attributes, 
           //after normalization sort the attributes on the basis of their relative scores
           getRelativeScore(positiveAttributesMap,relativeScorePositive);
           getRelativeScore(negativeAttributesMap,relativeScoreNegative);

           finalPositiveDocuments = bestPositiveDocuments;
           finalNegativeDocuments = bestNegativeDocuments;
           
           numberIterations++;
        }

        ArrayList<NewToken> bestPos = new ArrayList<NewToken>();
        ArrayList<NewToken> bestNeg = new ArrayList<NewToken>();

        //get the best set of attributes from the cluster
        getBestFeatures(relativeScorePositive,bestPos);
        getBestFeatures(relativeScoreNegative,bestNeg);

        System.out.println("printing the features for positive");
        printFeatures(bestPos,positiveFeatures);
        
        System.out.println();
        
        System.out.println("printing the features for negative");
        printFeatures(bestNeg,negativeFeatures);

        //create the "ranked" folder, if not exists
        //path where the ranked documents would be stored
        String rankedPath = getCurrentPath + "/src/mengg/data/ranked/";
        
        File rankedDir = new File(rankedPath);
        if(!rankedDir.exists()){
            rankedDir.mkdir();
        }
        
        //create the input "state" folder inside "ranked", if not exists
        //path to the input state in the ranked folder
        String rankedStatePath = getCurrentPath + "/src/mengg/data/ranked/"
                + statename;
        File rankedState = new File(rankedStatePath);
        if(!rankedState.exists()){
            if(rankedState.mkdir()){ 
                System.out.println("created the folder for " + statename);
            }
        }

        //creating path for "Positive" and "Negative" folders for the ranked "state"
        String postiveTrainingPath = rankedStatePath + "/Positive";
        String negativeTrainingPath = rankedStatePath + "/Negative";
        
        //first create set for positive and negative attributes, to be used for WEKA API
        createTrainingSet(finalPositiveDocuments,postiveTrainingPath,statename);
        createTrainingSet(finalNegativeDocuments,negativeTrainingPath,statename);
        
        System.out.println();
    }

    /**
     * method to print the best positive and the best negative features for this state
     * @param bestAttributes attributes to be printed
     * @param featureMap check if attribute already identified
     */
    private static void printFeatures(ArrayList<NewToken> bestAttributes, 
    		HashMap<String,Double> featureMap) {
		 
    	for(NewToken token: bestAttributes){
            System.out.print(token.word + " ");
            if(featureMap.containsKey(token.word)){
            	featureMap.put(token.word, token.count+featureMap.get(token.word));
            }
            else{
            	featureMap.put(token.word,token.count);
            }
        }
	}

	/**
     * method to get tokens from the best documents from cluster-based ranking,
     * a map is used to get the unique words from the documents with their counts and then
     * the map is converted to a list
     * @param rankedAttributes list of attributes to be obtained
     * @param documentTokenMap document map to get the list of tokens
     * @param bestDocuments the best documents obtained
     */
    private static void getTokensFromBestDocuments(
			ArrayList<NewToken> rankedAttributes,
			HashMap<String, ArrayList<NewToken>> documentTokenMap,
			ArrayList<String> bestDocuments) {
		
    	//create a map for the attributes, with their counts
    	HashMap<String, Double> attributeMap = new HashMap<String, Double>();
    	
    	//loop over all the best documents
    	for(String fileName: bestDocuments){
    		//get the list of terms present in it.
    		for(NewToken token : documentTokenMap.get(fileName)){
    			if(!attributeMap.containsKey(token.word)){
    				attributeMap.put(token.word, token.count);
    			}
    			else{
    				attributeMap.put(token.word,attributeMap.get(token.word) + token.count);
    			}
    		}
    	}
    	
    	//get the list of tokens from this map
    	rankedAttributes = refineMap(attributeMap);
	}

    /**
     * method to process attributes to remove terms having higher count in opposite cluster
     * @param attributesMap represents the cluster
     * @param rankedAttributes the list that is to be filtered
     * @return rankedAttributes the filtered list 
     */
	private static ArrayList<NewToken> processAttributes(HashMap<String, Double> attributesMap,
                                                         ArrayList<NewToken> rankedAttributes) {

        ArrayList<NewToken> resultAttributes = new ArrayList<NewToken>();
        for(NewToken token: rankedAttributes){
            if(!attributesMap.containsKey(token.word)){
                resultAttributes.add(token);
            }
        }
        return resultAttributes;
    }

    /**
     * method to generate list of tokens from the attribute map, or the initial cluster of
     * attributes to be used for scoring the paragraphs
     * @param attributesMap the map to get tokens from (NewToken)
     * @return ArrayList<NewToken> the generated list of NewToken tokens
     */
    private static ArrayList<NewToken> refineMap(HashMap<String, Double> attributesMap) {
        ArrayList<NewToken> arrayList = new ArrayList<NewToken>();
        for(String s: attributesMap.keySet()){
            NewToken token = new NewToken();
            token.word = s;
            token.count = attributesMap.get(s);
            arrayList.add(token);
        }
        return arrayList;
    }

    /*private static ArrayList<NewToken> getBestAttributes(ArrayList<NewToken> attributesList) {

        ArrayList<NewToken> resultAttributes = new ArrayList<NewToken>();
        int count=0;
        for(NewToken token: attributesList){
            resultAttributes.add(token);
            count++;
            if(count==attributesList.size()/denominatorBestAttributes){
                break;
            }
        }
    return resultAttributes;
    }*/



    /*private static boolean noMoreIterations(HashMap<String, Double> relativeScorePositive,
                                              HashMap<String, Double> relativeScoreNegative) {

        boolean testIterations;
        int count = 0;
        boolean positive=true;
        for(String s:relativeScorePositive.keySet()){
            if(relativeScorePositive.get(s)<scoreThreshold){
                positive=false;
                break;
            }
            if(count==bestResultCount && positive==true){
                break;
            }
            count++;
        }
        count=0;
        boolean negative=true;
        for(String s:relativeScoreNegative.keySet()){
            if(relativeScoreNegative.get(s)<scoreThreshold){
                negative=false;
                break;
            }
            if(count==bestResultCount && negative==true){
                break;
            }
            count++;
        }
        return positive && negative;
    }*/

    /**
    * Method to get the relative score of the attributes
    * @param attributesMap map to be scored
    * @param relativeScoreMap map obtained after scoring
    * */
    private static void getRelativeScore(HashMap<String, Double> attributesMap,
                                         HashMap<String, Double> relativeScoreMap) {

        double denominator = 0.0;
        for(String words: attributesMap.keySet()){
            denominator+=attributesMap.get(words);
        }
        for(String words: attributesMap.keySet()){
            relativeScoreMap.put(words,attributesMap.get(words)/denominator);
        }
    }

    /**
     * update the clusters, if the term is already present in the cluster,
     * increase its count by its count in the ranked attribute list
     * @param attributesMap map of attributes to be updated
     * @param rankedAttributes list of attributes to be added
     */
    private static void updateAttributesMap(HashMap<String, Double> attributesMap,
                                            ArrayList<NewToken> rankedAttributes) {

        for(NewToken token:rankedAttributes){
            if(!attributesMap.containsKey(token.word)){
                attributesMap.put(token.word,token.count);
            }
            else{
                Double val = attributesMap.get(token.word);
                attributesMap.put(token.word,val+token.count);
            }
        }
    }

    /**
     * method to create a map from the lists, based on the relative counts of 
     * same terms in the opposite lists
     * @param attributesMap the map to be populated
     * @param attributesList1 the first list
     * @param attributesList2 the second list
     */
    private static void addToAttributesMap(HashMap<String, Double> attributesMap,
                                           ArrayList<NewToken> attributesList1,
                                           ArrayList<NewToken> attributesList2) {

        for(NewToken token:attributesList1){
            boolean toAdd = true;
            for(NewToken token1: attributesList2){
                if(token.word.compareToIgnoreCase(token1.word)==0
                        && token.count < token1.count){
                    toAdd=false;
                    break;
                }
            }
            if(toAdd){
                attributesMap.put(token.word,token.count);
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
                String getPath = getCurrentPath + "/src/mengg/data/states/"
                                + state + "/" + section + ".txt";
                fileReader = new FileReader(new File(getPath));
                BufferedReader reader = new BufferedReader(fileReader);
                String line=null;

                String topDocPath = path + "/" + section + ".txt";
                File topFile = new File(topDocPath);
                FileWriter fileWriter = null;

                if(topFile.createNewFile()){
                    //fileWriter = new FileWriter(file);
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
                e.printStackTrace();  
            } catch (IOException e) {
                e.printStackTrace();  
            }
        }
    }

    /**
    * adding the filename and the scores to positive document map
    * and negative document map. Separating the document in terms of its
    * p-score and n-score
    * @param sectionDocuments list of documents
    * @param positiveDocumentsMap map for positive documents
    * @param negativeDocumentsMap map for negative documents
    * */
    private static void addToSectionMap(ArrayList<SectionDocument> sectionDocuments,
                                        HashMap<String, Double> positiveDocumentsMap,
                                        HashMap<String, Double> negativeDocumentsMap) {

        for(SectionDocument section : sectionDocuments){
        	negativeDocumentsMap.put(section.sectionName,section.nscore);
            positiveDocumentsMap.put(section.sectionName,section.pscore);
        }
    }

    /**
    * gets the best documents for the given seed sets
    * based on their p-scores and n-scores
    * @param senseDocumentsMap attributes map created
    * @param documentList list of the documents to be populated
    * */
    private static void getBestDocuments(HashMap<String, Double> senseDocumentsMap,
                                         ArrayList<String> documentList) {

        Map<String,Double> sortedDocPredictQuery = sortThis(senseDocumentsMap);
        Set<Map.Entry<String,Double>> sortedItr = sortedDocPredictQuery.entrySet();
        Iterator sortedIterator = sortedItr.iterator();

        int count=0;
        boolean sizeSmall = false;
        while(sortedIterator.hasNext() && count<bestResultCount && !sizeSmall){
        	
        	//check if the document size is less than bestResultCount
        	if(count==senseDocumentsMap.size()/2){
        		sizeSmall = true;
        	}
            String s =  sortedIterator.next().toString();
            String file = s.split("=")[0];
            //String pre = s.split("=")[1];
            documentList.add(file);
            count++;
        }
    }
    
    /**
     * gets the best features from the list, based on the counts
     * @param featureMap map of all attributes
     * @param bestFeatures list of best attributes to be stored
     * */
    private static void getBestFeatures(HashMap<String,Double> featureMap, 
    		ArrayList<NewToken> bestFeatures){
    	Map<String,Double> sortedMap = sortThis(featureMap);
    	Set<Map.Entry<String,Double>> sortedItr = sortedMap.entrySet();
    	Iterator sortedIterator = sortedItr.iterator();
    	
    	int count=0;
    	while(sortedIterator.hasNext() && count<bestResultCount){
    		String[] parts = sortedIterator.next().toString().split("=");
    		NewToken token = new NewToken();
    		token.word = parts[0];
    		token.count = Double.parseDouble(parts[1]);
    		bestFeatures.add(token);
    		count++;
    	}
    }

    /**
    * sort the map of the documents list based on their scores
    * @param listDocuments map to be sorted
    * @return Map<String, Double> sorted representation of the map
    * */
    private static Map<String, Double> sortThis(HashMap<String, Double> listDocuments) {
        DoubleValueComparator newValueComparator = new DoubleValueComparator(listDocuments);
        Map<String,Double> stringLongMap = new TreeMap<String,Double>(newValueComparator);
        stringLongMap.putAll(listDocuments);
        return stringLongMap;
    }

    /**
    * method to update the document scores based on the seed attributes
    * @param sectionDocuments section documents whose scores are to be updated
    * @param positiveAttributesList positive attributes list
    * @param negativeAttributesList negative attributes list
    * */
    private static void updateScores(ArrayList<SectionDocument> sectionDocuments,
                                     ArrayList<NewToken> positiveAttributesList,
                                     ArrayList<NewToken> negativeAttributesList) {

        for(SectionDocument section: sectionDocuments){
        	section.nscore = 0.0;
        	section.pscore = 0.0;
            //get the individual tokens from the section one by one
            for(NewToken token: section.termList){
                section.pscore += rankToken(token,positiveAttributesList);//calculate for positive attribute
                section.nscore += rankToken(token,negativeAttributesList);//calculate for negative attribute
            }
        }
    }

    /**
    * method to score the documents on the basis of seed attributes, matches each term with the attributes list
    * if match then return the sum of the count of the term and the count of the attribute term in the list
    * @param term the term in the section document which is to be ranked
    * @param attributesList list of attribute tokens
    * @return double score for this term
    * */
    private static double rankToken(NewToken term,
                                    ArrayList<NewToken> attributesList) {
        double score = 0.0;
        for(NewToken token : attributesList){
            if(term.word.compareToIgnoreCase(token.word)==0){
                score=token.count+term.count;
            }
        }
        return score;
    }

    /**
    * method to obtain the seed attributes from the seed collection
    * of the given state, uses the populate method to populate
    * the seeds and then processes the individual terms or attributes
    * @param positiveAttributesList stores the positive attributes
    * @param negativeAttributesList stores the negative attributes
    * @param state the input state
    * */

    private static void getSeedAttributes(ArrayList<NewToken> positiveAttributesList,
                                          ArrayList<NewToken> negativeAttributesList,
                                          String state) {

        String positiveSeedPath = getCurrentPath + "/src/mengg/data/seed/"
                                    + state + "/Positive";

        String negativeSeedPath = getCurrentPath + "/src/mengg/data/seed/"
                                    + state + "/Negative";

        populateSeedAttributes(positiveSeedPath, positiveAttributesList, state);
        populateSeedAttributes(negativeSeedPath, negativeAttributesList, state);

        for(NewToken token: negativeAttributesList){
            if(token.word.compareToIgnoreCase("state")==0 || token.word
                    .compareToIgnoreCase("states")==0){
                token.count=0;
            }
        }
        sort(positiveAttributesList);
        sort(negativeAttributesList);
    }

    /**
    * interface to sort the attribute list based on the scores
    * and lexicographically
    * @param attributesList list to be sorted
    * */
    private static void sort(ArrayList<NewToken> attributesList) {

        Collections.sort(attributesList, new Comparator<NewToken>() {
            @Override
            public int compare(NewToken newToken, NewToken newToken2) {

                if (newToken.count > newToken2.count) {
                    return -1;
                } else if (newToken.count < newToken2.count) {
                    return 1;
                } else {
                    return newToken.word.compareToIgnoreCase(newToken2.word);
                }
            }
        });
    }

    /**
    * method implementation to get the attributes from the seed folder
    * uses an array-list of tokens to get the positive and the negative
    * attributes
    * @param seedPath path to the seed folder
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

        if(file.isDirectory()){
            String[] listPath = file.list();
            for(String filePath: listPath){
                getAttributesImpl(new File(file,filePath),attributesList,wordList,state);
            }
        }
        else{
            getAttributesImpl(file,attributesList,wordList,state);
        }
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
                                          ArrayList<String> wordList, String state) {
    	state = InputHandler.combineString(state);
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
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        else {
        	System.err.println("cannot read this file");
        }
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
