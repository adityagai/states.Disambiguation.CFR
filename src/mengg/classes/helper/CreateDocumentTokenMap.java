package mengg.classes.helper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: prasannjit
 * Date: 11/6/13
 * Time: 9:07 PM
 * To change this template use File | Settings | File Templates.
 */
/** @author Prasannjit
* Used to create a document token map given the path and a state
* "state" name is used to filter out the state from the text
* */
public class CreateDocumentTokenMap {

    /**
    * @param state - state which is to be filtered from the text
    * @param dirPath - document token map for the directory to be created
    *
    * @return HashMap<String, ArrayList<NewToken>>
    * return the document token map for this directory, having the file name as the key
    * and the list of tokens in the file as the ArrayList
    * */
    public static HashMap<String, ArrayList<NewToken>> createDocumentTokenMap(String state,
                                                                              String dirPath){
        String directoryPath = dirPath + "/" + state;
        HashMap<String, ArrayList<NewToken>> documentTokenMap = new HashMap<String, ArrayList<NewToken>>(0);
        createTokenMapForDocument(directoryPath,documentTokenMap,state);
        return documentTokenMap;

    }

    /**
    * @param directoryPath - document token map for the directory to be created
    * @param documentTokenMap - documentTokenMap the document token map for this directory,
    * 															having the file name as the key
    * 															and the list of tokens in the file as the ArrayList
    * @param state - state which is to be filtered from the text
    * 
    *
    * */
    private static void createTokenMapForDocument(String directoryPath,
                                                  HashMap<String, ArrayList<NewToken>> documentTokenMap,
                                                  String state) {

        if(directoryPath==null || directoryPath.isEmpty()){
            System.err.println("check the path");
            System.exit(1);
        }

        File directoryFile = new File(directoryPath);

        if(!directoryFile.canRead()){
            System.err.println("Unable to read this file");
        }
        createTokenMapforDocumentImpl(directoryFile,documentTokenMap,state);
    }

    /**
    * Implementation for the above function
    * @param directoryFile - file to be mapped into tokens
    * @param documentTokenMap -the document token map for this directory,
																    having the file name as the key
																    and the list of tokens in the file as the ArrayList
    * @param state - state which is to be filtered from the text
    *
    *
    * */
    private static void createTokenMapforDocumentImpl(File directoryFile,
                                                      HashMap<String, ArrayList<NewToken>> documentTokenMap,
                                                      String state) {
        if (directoryFile.canRead()){

            if (directoryFile.isDirectory()) {
                String[] files = directoryFile.list();

                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        createTokenMapforDocumentImpl(new File(directoryFile, files[i]), documentTokenMap, state);
                    }
                }

            }
            else {
            	state = InputHandler.combineString(state);
                FileReader fis = null;
                ArrayList<NewToken> tokenDocumentList = new ArrayList<NewToken>(0);
                ArrayList<String> stringArrayList = new ArrayList<String>(0);

                String filename = directoryFile.getName();

                try {
                    fis = new FileReader(directoryFile);
                    BufferedReader reader = new BufferedReader(fis);
                    String line;

                    while((line=reader.readLine())!=null){

                        StringTokenizer tokenizer = new StringTokenizer(line);

                        while(tokenizer.hasMoreTokens()){
                            NewToken token = new NewToken();
                            token.word = tokenizer.nextToken();

                            //tokens should not have state term
                            if(token.word.compareTo(state)==0 && tokenizer.hasMoreTokens()){
                                token.word=tokenizer.nextToken();
                            }

                            //if this term has not be tokenized then tokenize it
                            if(!stringArrayList.contains(token.word)){

                                token.count=1;
                                stringArrayList.add(token.word);
                                tokenDocumentList.add(token);

                            }//term has already been read in this file, update its count in the text
                            else{

                                for(NewToken t:tokenDocumentList){
                                    if(t.word.compareToIgnoreCase(token.word)==0){
                                        t.count++;
                                        break;
                                    }
                                }
                            }

                        }
                    }

                //get the filename of this file, and store it in the map
                int pos = filename.indexOf(".txt");
                filename = filename.substring(0,pos);
                documentTokenMap.put(filename,tokenDocumentList);

                } catch (FileNotFoundException e) {
                    System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
                } catch (IOException e) {
                    System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
                }
            }
        }
    }
}