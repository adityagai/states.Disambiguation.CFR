package mengg.classes.main;

import mengg.classes.helper.InputHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: prasannjit
 * Date: 11/2/13
 * Time: 6:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class XMLParagraphParser {

    public static String getCurrentPath = new java.io.File("").getAbsolutePath();
    public static String partXMLPath = getCurrentPath + "/src/mengg/data/title-7/part-";
    public static String dataPath = getCurrentPath + "/src/mengg/data";
    public static String parentStatePath = dataPath + "/" + "states";
    public static String inputPath = dataPath + "/" + "input/states";

    /**
    * create section files for each of the part xml for this state
    * Input Parameters: partNo - part number of the part-xml
    * Input Parameters: state - name of the state in my states list
    * 
    * @param partNo  xml part no of the url
    * @param state  name of the state
    *
    * */
    public static void createSectionFile(String partNo,String state){

        String partXMLString = partXMLPath + partNo + ".xml";//get the part-xml file

        //create a file for this part xml file
        String statePath = parentStatePath + "/" + state;//location of the folder for this state
        
        
        File partXmlFile = new File(partXMLString);//get the file object for the input

        //state folder already created in CreateDirectoryStructure file
        if(partXmlFile.exists()){
            getSectionContent(partXmlFile, state, statePath);
        }
    }


    /**
    * get the section content for this part-xml file containing the reference to the "state"
    * for each "P" tag in the current section, create a new file in the "states" folder
    * for this "state"
    *
    * @param partXmlFile the xml file to be parsed
    * @param stateString name of the state
    * @param statePath folder where the paragraphs would be stored
    *
    * */
    public static void getSectionContent(File partXmlFile, String stateString, String statePath){

        try{

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(partXmlFile);
            document.getDocumentElement().normalize();

            //check if the state is present in other nodes
            //parse the "section" tags in the xml
            NodeList nodeList1 = document.getElementsByTagName("section");

            for(int i=0;i<nodeList1.getLength();i++){
                Node node = nodeList1.item(i);
                if(node.getNodeType()==Node.ELEMENT_NODE){

                    Element element = (Element)node;

                    //get the "head" tags from each "section"
                    NodeList headTags = element.getElementsByTagName("head");
                    Node headNode = headTags.item(0);

                    //if this is an element node
                    if(headNode.getNodeType()==Node.ELEMENT_NODE){
                        Element headElement = (Element)headNode;

                        //get the text content
                        String headText = headElement.getTextContent().trim();

                        //if the head contains the "state" string
                        if(headText.contains(stateString)){
                            String sectionURL = element.getElementsByTagName("num")
                                                .item(0).getTextContent().trim();

                            //sectionURL = sectionURL.replace(".","-");

                            sectionURL = statePath + "/" + sectionURL + "." + 0 + ".txt"; //create the paragraph name

                            createEntry(headText,sectionURL);
                        }
                    }

                    //get the "P" tags from each "section"
                    NodeList pTags = ((Element)element.getElementsByTagName("contents").item(0)).
                                      getElementsByTagName("P");

                    //iterating over the "P" tags
                    for(int k=0;k<pTags.getLength();k++){

                        Node node1 = pTags.item(k);

                        //get the text-content for each "P" tag
                        if(node1.getNodeType()==Node.ELEMENT_NODE){

                            Element element1 = (Element)node1;
                            String text = element1.getTextContent().trim();

                            //if the text contains the reference to the "state" then add it
                            if(text.contains(stateString)){

                                //get the section number
                                String sectionURL = element.getElementsByTagName("num")
                                                .item(0).getTextContent().trim();

                                //format the section number
                                //sectionURL=sectionURL.replace(".","-");

                                //create the section URL
                                sectionURL = statePath + "/" + sectionURL + "." + (k+1) + ".txt";

                                createEntry(text,sectionURL);

                            }
                        }
                    }
                }
            }

        } catch (ParserConfigurationException e) {
            System.out.println(e.getLocalizedMessage());
        } catch (SAXException e) {
            System.out.println(e.getLocalizedMessage());
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }

    }

    /**
    * method that writes the paragraph
    * @param text text to be written
    * @param sectionURL destination where the content is written
    * 
    * */
    private static void createEntry(String text, String sectionURL) {

        StringBuilder stringBuilder = new StringBuilder();

        File sectionFile = new File(sectionURL);
        FileWriter fileWriter = null;

        try {

            //create a new file for this section and start a writer on it
            if(sectionFile.createNewFile()){
                fileWriter = new FileWriter(sectionFile);
            }

            //get the text content for this "state", if in multiple lines
            //process each of the lines, removing the end-line and replace with spaces
            if(sectionFile.exists()){
                if(text.contains("\n")){
                    String[] strings = text.split("\n");

                    StringBuilder stringBuilder1 = new StringBuilder();

                    for(int i1=0;i1<strings.length;i1++){
                        stringBuilder1.append((strings[i1].trim()) + " ");
                    }

                    text=stringBuilder1.toString();
                    stringBuilder.append(text + "\n");
                }
                else{
                    stringBuilder.append(text + "\n");
                }

                stringBuilder = filterSection(stringBuilder);
                if(fileWriter!=null)
                    fileWriter.append(stringBuilder);
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
    }

    /**
    * method to remove special characters from the input string,
    * remove numbers from the text of the section
    * remove urls from the text of the section
    * @param builderString  text to be filtered
    *
    * @return StringBuilder return the filtered text
    * 
    * */
    public static StringBuilder filterSection(StringBuilder builderString) {

        StringTokenizer tokenizer = new StringTokenizer(builderString.toString());
        String result="";
        StringBuilder builder = new StringBuilder();

        while (tokenizer.hasMoreTokens()){
            String word = tokenizer.nextToken();

            //handle special word in the input
            if(InputHandler.isSpecialWord(word)){
                word=word+tokenizer.nextToken();
            }
            result = result + word + " ";
        }

        //handle the stop words and special characters in the text
        result=InputHandler.filterString(result);

        builder.append(result);

        return builder;
    }

    /**
    * create section files for each of the part xml for this state
    * Input Parameters: partNo - part number of the part-xml
    * Input Parameters: state - name of the state in my states list
    * 
    * @param partNo  xml part no of the url
    * @param state  name of the state
    *
    * */
    public static void AddInputFile(String partNo,String state){

        String partXMLString = partXMLPath + partNo + ".xml";//get the part-xml file

        String statePath = inputPath + "/" + state;//location of the folder for this state

        File partXmlFile = new File(partXMLString);//get the file object for the input

        //state folder already created in CreateDirectoryStructure file
        if(partXmlFile.exists()){
            getSectionContent(partXmlFile, state, statePath);
        }
    }
}
