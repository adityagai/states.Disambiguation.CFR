Readme

1. Add the the jar files "lookup_lib.jar" and "weka-3.6.0.jar" in the project build path. These files are needed to run the SOLR based java application and WEKA based java files.



2. After adding the jars, configure the following data-paths in your project.
	a)Name the project as "mengg". So that the folder structure becomes  "/src/mengg"
	b)In the "mengg" folder create the following data-folders, "seed" and "title-7", to store the data-files for the seed and the part-xmls respectively.
	c)Create a folder "classes" in the "mengg" folder to store the java classes being used in the project.
	d)Create an empty folder "tests" in the "data" folder. Inside the "tests" folder create the following folders:
		1)logs
		2)trainingModel
		3)training_arff

	Please note that these should be empty sub-folders inside the folder "tests" and would get populated with the results from WEKA

	e)Folder structure should be as given below:
		1)Classes : stored at "src/mengg/classes"
		2)Data: 
			<COPY-AS-SUCH, FIXED>
			src/mengg/data/title-7 - to be copied from the data files
			src/mengg/data/seed - to be copied from the data files
			src/mengg/data/tests/testingData - to be copied from the data files. Consists of the data to be tested against.
			src/mengg/data/input/csvfolder - contains the gold-standard csv files. 
			src/mengg/data/stop.txt - contains the stop-words list being used in the algorithm.
			
			<CREATED, EMPTY>
			src/mengg/data/tests/logs - empty folder
			src/mengg/data/tests/trainingModel - empty folder
			src/mengg/data/tests/training_arff - empty folder
			src/mengg/data/tests/testing_arff - empty folder
			src/mengg/data/tests/output - empty folder

	d)Delete the following data folders from the project, if present:
		1) "data/states"
		2) "data/input"
		These files are generated as a part of the application run



3. Run the application, TrainingMain.java. The following data-files are created:
	a)urlfolder - For each state, a url-folder is created, containing the urls obtained from the SOLR engine. This would prevent the application to call the SOLR engine on every run to fetch the urls online.
	Location - "src/mengg/data/urlfolder"
	
	b)states - the parent folder "states" that would contain the training data for each of the state.
	Location - "src/mengg/data/states"

	c)Inside the "states" folder, entries are created for each of the input state. For example, a folder "Michigan" would be created at the location, 
	"src/mengg/data/states/Michigan"

	d)parafolder - the paragraph-ids obtained for all the states obtained above are stored in a "parafolder" created at the location,
	"src/mengg/data/parafolder"

	e)ranked - the parent folder that contains the training-data for the input states into "pos" and "neg" folders. For example, for the state "Michigan",
	it would have the entry "src/mengg/data/ranked/Michigan/pos" for the paragraphs classified as the positive instances.

	f)input - the parent folder that would contain the data-folders for the unseen states.
	Also contains- "positive.txt" that lists the positive attributes retrieved from the feature extraction algorithm and "negative.txt" that contains the negative attributes retrieved from the feature extraction algorithm

	g)The data-folder "src/mengg/data/tests/training_arff" is populated with the arff data based on the classification done in the "ranked" folder.

	h)The data-folder "src/mengg/data/tests/trainingModel" is populated with the model files for the training data.

	i)The data-folder "src/mengg/data/tests/logs" is populated with the results of the WEKA training algorithm

	

4. Run the application, TestingMain.java. Give the name of the "seen" state as the input. For example - "New York". 
Gives the test results of the classification for the test paragraph instances. Creates the following data-files
	1)The data-folder "src/mengg/data/tests/testing_arff" is populated with the arff data for the testing data present in the data-folder
	"src/mengg/data/tests/testingData" 

	2)The data-folder "src/mengg/data/tests/output" is populated with the testing results for the given state.



5. Run the application, RankInputImpl.java. Give the name of the "unseen" state as the input. For example - "Kentucky".
Gives the results based on comparison with the human-generated gold-standard csv files. 
Creates the following data-files in the "input" folder:

	a)urlfolder - For each "unseen" state, a url-folder is created, containing the urls obtained from the SOLR engine. This would prevent the application to call the SOLR engine on every run to fetch the urls online.
	Location - "src/mengg/data/input/urlfolder"
	
	b)states - the parent folder "states" that would contain the paragraphs for each of the "unseen" state.
	Location - "src/mengg/data/input/states"

	c)Inside the "states" folder, entries are created for each of the input "unseen" state. For example, a folder "Kentucky" would be created at the location, 
	"src/mengg/data/input/states/Kentucky"

	d)parafolder - the paragraph-ids obtained for all the states obtained above are stored in a "parafolder" created at the location,
	"src/mengg/data/input/parafolder"

	e)ranked - the parent folder that contains the paragraphs for the "unseen" states into "pos" and "neg" folders. For example, for the state "Kentucky",
	it would have the entry "src/mengg/data/input/ranked/Kentucky/pos" for the paragraphs classified as the positive instances.

