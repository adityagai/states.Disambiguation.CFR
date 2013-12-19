package mengg.classes.main;

import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.TextDirectoryLoader;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author adityagai
 * 
 * This class makes use of Weka API with SVM approach for classifying unseen state instances.
 * It generates the arff file for that state on the basis of the folders. The arff file is used for build classifier for Support Vector Machines
 * with N-grams which are used as a model for predicting instances. 
 * It also generates output logs which specifies whether an instance is a positive or a negative instance.
 * 
 * Input :
 * 		 - Console input specifying a particular state   
 * 		 - Positive & Negative for folders that particular state
 * Output :
 * 		 - Output arff file containing the instances.   
 * 		 - Text file containing the predictions
 * 		  
 */
public class TestingMain {
	
	// List of all the states for which the tests can be performed
    public static List<String> states = Arrays.asList("California",
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
            "Washington");
    
    
	public static void main(String[] args)
	{
			String state;
			System.out.print("Enter the state to test for : ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			
		    try {
		         state = br.readLine();
                 //state = args[0];
                 System.out.println(state);
	
				 Boolean validState = false;
				 for(String st : states){
					 if(st.equalsIgnoreCase(state)){
						 state = st;
						 validState = true;
						 break;
					 }
		         }
		         if(!validState){
		        	 System.out.println("No such state is present currently..!!");
		        	 throw new Exception();
		         }

                String getCurrentPath = new java.io.File("").getAbsolutePath();
                
                // Paths which are set for different input/output folders
		          String testDir = getCurrentPath + "/src/mengg/data/tests/testingData/"+state;
                  String testOutputPath=getCurrentPath + "/src/mengg/data/tests/testing_arff/Test_"+state+".arff";
                  String trainArffPath = getCurrentPath + "/src/mengg/data/tests/training_arff/training_" + state + ".arff";
                  String outputPath = getCurrentPath + "/src/mengg/data/tests/output/output_" + state + ".txt";

		          Instances train = new Instances(new BufferedReader(new FileReader(trainArffPath)));
		          Instances test = getInstancesFromDir(testDir,testOutputPath);
		         
		         
		           train.setClassIndex(test.numAttributes()- 1);
		            
		 		    // Applying StringToWordVector to the directory
		    		StringToWordVector filter = new StringToWordVector();
		    		filter.setInputFormat(train); 
				    filter.setOutputWordCounts(true);
				    filter.setLowerCaseTokens(true);
				    
				    // Applying NGram Tokenizer to StringToWord vector
				    NGramTokenizer ngrams = new NGramTokenizer();
				    ngrams.setNGramMinSize(2);
				    ngrams.setNGramMaxSize(4);
				    filter.setTokenizer(ngrams);
				    
				    // Applies filter to the training data
				    train = Filter.useFilter(train, filter);  			
		 		   
				    
				    // Applying SMO Support Vector Machines algorithm with Polykernel options 
				    SMO scheme = new SMO();
				    scheme.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\""));
				    scheme.buildClassifier(train);
		 		    
		 	       Instances newTest = Filter.useFilter(test, filter);
		 	       System.out.println("-------------- Output ------------------------\n");
		 	       Instances labeled = new Instances(newTest);
		 	       
		 	      
		 	       BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
		 	       writer.write("--------- Predictions made for state of "+state+" ------------\n\n");
		 	      
		 	       // Predicts instances for classifying them into state or non-state.
			 	   for (int i = 0; i < newTest.numInstances(); i++) {
				 		double clsValue = scheme.classifyInstance(newTest.instance(i));
						labeled.instance(i).setClassValue(clsValue);
						String className=labeled.classAttribute().value((int)clsValue);
						System.out.println(checkClass(test.instance(i))+":\nBased on Testing should be in: "+className);
						System.out.println();
						writer.write(test.instance(i)+" : "+className+ "\n");
				 	 }
		 	 
			 	    writer.flush();
		 			writer.close();
		         
		      } catch (Exception e) {
		         System.out.println("\nError occured..please try again..!!");
		         System.exit(1);
		      }
	}
	
	/**
	 * @param instance
	 * @return String
	 * checks if the instance is example of a positive or negative instance
	 */
	private static String checkClass(Instance instance) {
		String instanceString = instance.toString();
		if(instanceString.contains("Negative") || instanceString.contains("negative")){
			System.out.println("Present in Tests- Negative");
		}
		if(instanceString.contains("Positive") || instanceString.contains("positive")){
			System.out.println("Present in Tests - Positive");
		}
		return instance.toString();
	}

	/**
	 * @param dirPath
	 * @param outputPath
	 * @return Instances
	 * @throws IOException
	 * 
	 * This function is used to generate arff file from the Positive and Negative instance folders which are
	 * provided by the user.
	 */
	public static Instances getInstancesFromDir(String dirPath, String outputPath) throws IOException{
			// Converts the directory into a dataset
			 TextDirectoryLoader loader = new TextDirectoryLoader();
			 loader.setDirectory(new File(dirPath));
		     
			 // Creates an instance set for the given dataset.
			 Instances instances = loader.getDataSet();
			 PrintWriter out = new PrintWriter(outputPath);
			    
			 out.println(instances);
			 out.close();
			
			 return instances;
	   }	
}
