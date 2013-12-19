package mengg.classes.main;

import mengg.classes.helper.CreateDirectoryStructure;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.ClassifierSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.TextDirectoryLoader;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.*;
import java.util.*;

/**
 * @author adityagai
 * 
 * This class makes use of Weka API with SVM approach for generating training models for each of the states.
 * It generates the arff file for that state on the basis of the folders. The arff file is used for build classifier for Support Vector Machines
 * with N-grams which are used as a model for predicting instances.
 * It also generates logs determining the predictions, accuracy and confusion for 10-fold cross validation approach.
 *  
 * Input :
 * 		 - Positive & Negative for folders that particular state
 * Output :
 * 		 - Output arff file containing the instances.   
 * 		 - Models for each of the states.
 * 		 - Logs containing 10-fold cross validation approach 
 * 		  
 */
/**
 * @author adityagai
 *
 */
public class TrainingMain {

    public static String getCurrentPath = new java.io.File("").getAbsolutePath();
    
    // List of all the states for which models can be generated.
    public static List<String> states =
            Arrays.asList("California",
                            "Connecticut",
                            "Florida",
                            "Idaho",
                            "Maine",
                            "Michigan",
                            "New Jersey",
                            "New York",
                            "North Dakota",
                            "Pennsylvania",
                            "South Carolina",
                            "Washington"
            );
	

	public static void main(String args[]) throws IOException {

        // Create the folder structure and return the list of states for which the folder
        // was created
        ArrayList<String> listOfStates = CreateDirectoryStructure.createStructureList(states);

        //  Create Training Data for each of the states in my list
        // uses SOLR to get the xmls and then parses the part xmls to create paragraphs for the states
        // that is then used for training
        CreateTrainingData.createTrainingData(listOfStates);

        HashSet<String> positiveFeatures = new HashSet<String>();//resulting positive features
        HashSet<String> negativeFeatures = new HashSet<String>();//resulting negative features
        
        HashMap<String,Double> positiveMap = new HashMap<String,Double>();
        HashMap<String,Double> negativeMap = new HashMap<String,Double>();
        
        try {
                //iterate through each of the states
				for (String state : states){

                    RankParagraphsImpl.rankDocumentForState(state,positiveMap,negativeMap);
                    
                    String newFolderPath = getCurrentPath +"/src/mengg/data/ranked/" + state;
                    String modelPath = saveTrainingModel(newFolderPath,state);
					
					System.out.println("\n------------------------ModelPath---------------------- : \n"+modelPath);
				}
            
            for(String s:positiveMap.keySet()){
            	if(negativeMap.containsKey(s)){
            		if(negativeMap.get(s)<positiveMap.get(s))
            			negativeMap.put(s,0.0);
            	}
            }
            
            for(String s:positiveMap.keySet()){
            	positiveFeatures.add(s);
            }
            
            for(String s:negativeMap.keySet()){
            	if(negativeMap.get(s)>0.0){
            		negativeFeatures.add(s);
            	}
            }

            String positivePath =  getCurrentPath+"/src/mengg/data/input/positive.txt";
            String negativePath = getCurrentPath + "/src/mengg/data/input/negative.txt";

            storeFeatures(positiveFeatures,positivePath);
            storeFeatures(negativeFeatures,negativePath);
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
    /**
    * @param features positive attributes from the classifier
    * @param path file where the features are to be stored
    * 
    * This method is used to store the features into a text file.
    * */
    private static void storeFeatures(HashSet<String> features, String path) {
        FileWriter writer = null;
        StringBuilder sb = new StringBuilder();

        File newFile = new File(path);
        try {
            if(newFile.createNewFile()){
                writer = new FileWriter(newFile);
            }
            if(newFile.exists()){
                for(String s:features){
                    sb.append(s + " ");
                }
                if(writer!=null){
                    writer.append(sb);
                }
            }

        }
        catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        if(writer!=null){
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    /**
     * @param instances
     * @return 
     * 
     * This method is used to apply the Preprocessing on the text:
     * - Applies Lowercase filter
     * - Creates an N-gram tokenizer (Min-2, Max-4)
     * - Applies the filter on the instances
     */
    public static Instances applyPreprocessFilter(Instances instances) {
		 
		try {
	    		//apply the StringToWordVector
	    		StringToWordVector filter = new StringToWordVector();
	    		filter.setInputFormat(instances); 
			    filter.setOutputWordCounts(true);
			    filter.setLowerCaseTokens(true);
			    
			    //apply the NGram Tokenizer to the String to Word vector
			    NGramTokenizer ngrams = new NGramTokenizer();
			    ngrams.setNGramMinSize(2);
			    ngrams.setNGramMaxSize(4);
			    filter.setTokenizer(ngrams);
			    
			    instances = Filter.useFilter(instances, filter);  			
	
			} catch (Exception e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		return instances;
	}
	
    /**
     * @param trainPath
     * @param state
     * @return String path where model is stored
     * 
     * This method is used to create and save the training model in .model files.
     * 
     */
	public static String saveTrainingModel(String trainPath, String state) throws Exception {
		
		// convert the directory into a dataset
        String getCurrentPath = new java.io.File("").getAbsolutePath();
        TextDirectoryLoader loader = new TextDirectoryLoader();
        loader.setDirectory(new File(trainPath));
        Instances train = loader.getDataSet();
        PrintWriter out = new PrintWriter(getCurrentPath + "/src/mengg/data/tests/training_arff/training_" + state + ".arff");
        out.println(train);
	    out.close();
		
	    // Loads the instances and applies preprocessing filter.
	    Instances trainFiltered = applyPreprocessFilter(train);
	     
	    
	    // Applies SMO SVM algorithm
	    SMO scheme = new SMO();			   
	    scheme.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));
	    
	    System.out.println("\n\nNumber of attributes :"+trainFiltered.numAttributes());
	    
	    // Builds classifier on the instances.
	    scheme.buildClassifier(trainFiltered);
	    
        String modelPath =  getCurrentPath + "/src/mengg/data/tests/trainingModel/" + state + ".model";
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(modelPath));
	    oos.writeObject(scheme);
	    oos.flush();
	    oos.close();
	    
	    
	  
	    System.out.println("\n\n ========================== Classifier model ============================= \n\n" + scheme);
	    
	    // Performs 10-fold cross validation on the instances.
	    Evaluation eTest = new Evaluation(trainFiltered);
        Random rand = new Random(1);
        int folds = 10;
        eTest.crossValidateModel(scheme, trainFiltered, folds, rand);
	    
        
        // Displays and stores the results
        PrintWriter log = new PrintWriter(getCurrentPath + "/src/mengg/data/tests/logs/" + state + ".log");
        log.print("------------------ Evaluation model for "+state+" ---------------------");
        log.println("\n\nNumber of attributes : "+trainFiltered.numAttributes());
        log.println(eTest.toSummaryString());
        log.println("=========== Confusion Matrix ===================");
        
        System.out.println(eTest.toSummaryString());
        System.out.println("=========== Confusion Matrix ===================");
        
        double[][] cmMatrix = eTest.confusionMatrix();
        for(int row_i=0; row_i<cmMatrix.length; row_i++){
            for(int col_i=0; col_i<cmMatrix.length; col_i++){
                System.out.print(cmMatrix[row_i][col_i]);
                System.out.print("|");
                log.print(cmMatrix[row_i][col_i]);
                log.print("|");
            }
            System.out.println();
            log.println();
        }	
        
        log.println("\n\n----------- ModelPath : "+modelPath);
        log.close();
        return modelPath;
        
	}


	/**
	 * @param data
	 * @param cls
	 * @return
	 * 
	 * This method applies the pre-processing filter to remove unnecessary attributes from the features.
	 * 
	 */
	public static Instances removeAttributes(Instances data, Classifier cls) {
		Instances instNew = data;
		try {
				AttributeSelection attsel = new AttributeSelection();
			    ClassifierSubsetEval cfsSubEval = new ClassifierSubsetEval();
			    cfsSubEval.setClassifier(cls);
			    cfsSubEval.setUseTraining(true);
			    BestFirst bfs = new BestFirst();
			    
			    attsel.setEvaluator(cfsSubEval);
			    attsel.setSearch(bfs);
				attsel.SelectAttributes(data);
			    int ind[] = attsel.selectedAttributes();
			    
			    int i;
			    for(i=0; i<ind.length; i++){
			    	System.out.println("\n\nIndices : "+ ind[i]);
			    }
			    
			    Remove remove = new Remove();
			    remove.setAttributeIndicesArray(ind);
			    remove.setInvertSelection(true);
			    remove.setInputFormat(data);
			    
			    instNew = Filter.useFilter(data, remove);
			    System.out.println("New Instances : "+instNew);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		
		    return instNew;
	}

}
