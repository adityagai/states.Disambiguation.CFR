package mengg.classes.main;

import mengg.classes.helper.InputHandler;
import mengg.classes.solr.GetXMLURLs;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: prasannjit
 * Date: 10/2/13
 * Time: 8:31 PM
 * To change this template use File | Settings | File Templates.
 */

/**
* Main class to parse the content, uses the SOLR Matcher to get the URLs for the input "state"
* These URLs are then parsed and paragraphs are extracted
* */

 public class CreateTrainingData {

        public static String getCurrentPath = new java.io.File("").getAbsolutePath();

        /**
        * @param listOfStates
        * the list of the states for which the data is to be created
        *
        * */
        public static void createTrainingData(List<String> listOfStates) throws IOException {

            if(listOfStates!=null){

                //iterating over the states in my list
                for(Iterator<String> it=listOfStates.iterator();it.hasNext();){

                    HashSet<String> partURLNoSet = new HashSet<String>();
                    String stateString = it.next();

                    ArrayList<String> xmlURLStrings = readXMLUrls(stateString);//get the part-xml files for this state
                    
                    if(xmlURLStrings==null){
                        break;
                    }

                    int i;
                    String partNumber;

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
                            XMLParagraphParser.createSectionFile(partNumber, stateString);
                            partURLNoSet.add(partNumber);
                        }
                    }
                    writeParagraphs(stateString);
               }
            }
        System.out.println("program done!");
    }

    /**
    * method to get the part number from the XML URL
    * @param xmlString the url to be processed, get the part-numbers only
    *
    * @return String
    * the processes url is returned
    * */
    private static String processXMLString(String xmlString) {

        String result = xmlString.substring(xmlString.indexOf("text/7/")+7);

        if(result.contains("/")){
            result = result.split("/")[0];
        }
        if(result.contains("part-")){
            result = result.split("part-")[1];
        }

        return result;
    }

    /**
     *create the entry for this state in the para-folder,
    * entry consists of all the URLs obtained from the SOLR engine
    *
    * @param state
    * 
    * */
    private static void writeParagraphs(String state) {

        String paraFolderPath = getCurrentPath + "/src/mengg/data/" + "parafolder";
        File paraFolderFile = new File(paraFolderPath);

        if(!paraFolderFile.exists()){
            if(paraFolderFile.mkdir()){
                System.out.println("folder parafolder created");
            }
        }

        String paraPath = paraFolderPath + "/" + state + ".txt";

        String statesPath = getCurrentPath + "/src/mengg/data/states/" + state;
        File statesFolder = new File(statesPath);

        try{
            if(statesFolder.isDirectory()){
                if(statesFolder.list()!=null){

                    //open my writer
                    FileWriter writer = new FileWriter(paraPath);

                    //getting the names of all the files present for the current state in "states" data folder
                    for(String paraFileName : statesFolder.list()){
                        writer.append(paraFileName);
                        writer.append("\n");
                    }
                    writer.close();
                }
            }
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
    * Method to write the XML Urls obtained from the SOLR engine  into url folder
    * For this state, get all the urls from the SOLR engine, and write them
    * into a folder, url-folder, to avoid calling the SOLR everytime for getting the URLs
    * @param state
    * 
    *
    * @return ArrayList<String>
    * read all the urls for this state, store and return it, to be used next
    * */
    private static ArrayList<String> readXMLUrls(String state) {

        //creating a state-url folder in the data
        String path = getCurrentPath + "/src/mengg/data/" + "urlfolder";
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
                    urlsList = GetXMLURLs.getXMLUrls(state); //warning, ignored
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
            System.out.println(e.getLocalizedMessage());
        }
        return urlsList;
    }
}
