package mengg.classes.main;

import mengg.classes.helper.CreateDirectoryStructure;
import mengg.classes.solr.GetXMLURLs;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: prasannjit
 * Date: 11/25/13
 * Time: 8:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class TrainingDataInput {
    public static String getCurrentPath = new java.io.File("").getAbsolutePath();

    /**
    * Method to parse the content, uses the SOLR Matcher to get the URLs for the input "state"
    * These URLs are then parsed and paragraphs are extracted
    * @param state "state" for which the training data is created
    * */
    public static void trainingDataInput(String state) throws IOException {

        //create the directory structure
        state = CreateDirectoryStructure.createStructureInputState(state);
        
        HashSet<String> partURLNoSet = new HashSet<String>();

        ArrayList<String> xmlURLStrings = readXMLUrls(state);


        int i;
        String partNumber;

        String[] stringBuilder = null;

        //iterating over the xml strings list
        for(i=0;i<xmlURLStrings.size();i++){
            
        	//get the section number
            partNumber= processXMLString(xmlURLStrings.get(i));

            if(partNumber.contains("-")){
                partNumber=partNumber.split("-")[0];
            }

            if(partNumber.contains(".")){
                partNumber=partNumber.substring(0,partNumber.indexOf("."));
            }

            //if number and not already parsed, parse the respective xml
            if(!partNumber.matches("[\\p{Alpha}]+") && !partURLNoSet.contains(partNumber)){
            	XMLParagraphParser.AddInputFile(partNumber, state);
            	partURLNoSet.add(partNumber);
            }
        }
        //write the paragraphs to a file
        writeParagraphs(state);
        System.out.println("program done!");    //}

        }

    /**
     * write the list of paragraphs obtained for this state
     * @param state input "state"
     */
    private static void writeParagraphs(String state) {

        String paraFolderPath = getCurrentPath + "/src/mengg/data/input/" + "parafolder";
        File paraFolderFile = new File(paraFolderPath);
        if(!paraFolderFile.exists()){
            if(paraFolderFile.mkdir()){
                System.out.println("folder parafolder created");
            }
        }
        String paraPath = paraFolderPath + "/" + state + ".txt";

        String statesPath = getCurrentPath + "/src/mengg/data/input/states/" + state;
        File statesFolder = new File(statesPath);
        try{
            if(statesFolder.isDirectory()){
                if(statesFolder.list()!=null){
                    //open my writer
                    FileWriter writer = new FileWriter(paraPath);
                    for(String paraFileName : statesFolder.list()){
                        writer.append(paraFileName);
                        writer.append("\n");//System.out.println(paraFileName);
                    }
                    writer.close();
                }
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    
    /**
    * Method to write the XML Urls obtained from the SOLR engine  into url folder
    * if the URLs are present already read the URLs directly from the file
    * @param state input "state"
    * @return ArrayList<String> list of xml-urls from SOLR
    * */
    private static ArrayList<String> readXMLUrls(String state) {
        
    	//creating a state-url folder in the data
        String path = getCurrentPath + "/src/mengg/data/input/" + "urlfolder";
        ArrayList<String> urlsList = new ArrayList<String>();
        File folderFile = new File(path);
        if(!folderFile.exists()){
            if(folderFile.mkdir()){
                System.out.println("Folder urlfolder created in data");
            }
        }
        String urlState = path + "/" + state + ".txt";
        File urlFile = new File(urlState);
        try{
            if(!urlFile.exists()){
                    if(urlFile.createNewFile()){
                        
                    	//new file created so write the xmls
                        urlsList = GetXMLURLs.getXMLUrls(state); //get xml-urls from the SOLR
                        FileWriter writer = new FileWriter(urlFile);
                        for(String url: urlsList){
                            writer.append(url);
                            writer.append("\n");
                        }
                        writer.close();
                    }
            }
            else{
                
            	//read from the url file
                urlsList = new ArrayList<String>();
                FileReader reader = new FileReader(urlFile);
                BufferedReader brReader = new BufferedReader(reader);
                String url;
                while((url=brReader.readLine())!=null){
                    urlsList.add(url);
                }
            }

        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());  //To change body of catch statement use File | Settings | File Templates.
        }
        return urlsList;
    }

    /**
     * method to process the xml-string to get the part number
     * @param xmlString xml-url
     * @return String part-no from the xml-url
     */
    private static String processXMLString(String xmlString) {
        {

            String result = xmlString.substring(xmlString.indexOf("text/7/")+7);

            if(result.contains("/")){
                result = result.split("/")[0];
            }
            if(result.contains("part-")){
                result = result.split("part-")[1];
            }

            return result;
        }
    }

}
