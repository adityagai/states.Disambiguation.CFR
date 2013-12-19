package mengg.classes.helper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created with IntelliJ IDEA.
 * User: prasannjit
 * Date: 11/28/13
 * Time: 2:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class InputHandler {
    public static String getCurrentPath = new java.io.File("").getAbsolutePath();
    public static String stopPath = getCurrentPath + "/src/mengg/data/stop.txt";

    /**
     * method to check if the input contains special words,
     * if yes then combine the words, to get a single word, "New York" becomes "NewYork"
     * @param input
     * @return input with the special words processed in the text
     */
    public static String specialWordHandler(String input){
        StringTokenizer tokenizer = new StringTokenizer(input);
        String result="";
        while(tokenizer.hasMoreTokens()){
            String word = tokenizer.nextToken();
            if(isSpecialWord(word) && tokenizer.hasMoreTokens()){
                word+=tokenizer.nextToken();
            }
            result+=word + " ";
        }

        return result.trim();
    }

    /**
     * method to check if this word is a special word
     * special words are words which should appear as single unit, eg "New York"
     * @param word
     * @return if special or not
     */
    public static boolean isSpecialWord(String word){
        if(word.compareTo("North")==0
                || word.compareTo("South")==0
                || word.compareTo("New")==0
                || word.compareTo("East")==0
                || word.compareTo("West")==0
                || word.compareTo("Central")==0
                || word.compareTo("San")==0
                || word.compareTo("El")==0
                || word.compareTo("United")==0
                || word.compareTo("Northern")==0
                || word.compareTo("Eastern")==0
                || word.compareTo("Western")==0
                || word.compareTo("Southern")==0
                ){
            return true;
        }
        return false;
    }

    /**
     * remove stop words from the input
     * @param input
     * @return input text with the stop-words removed
     */
    public static String handleStopWords(String input){
        FileReader reader;
        StringBuilder result = new StringBuilder();
        try {
            reader = new FileReader(stopPath);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String parts[] = bufferedReader.readLine().split(" ");

            StringTokenizer tokenizer = new StringTokenizer(input);
            while(tokenizer.hasMoreTokens()){
                String word = tokenizer.nextToken();
                boolean match = false;
                for(String stop: parts){
                    if(stop.compareToIgnoreCase(word)==0){
                        match=true;
                        break;
                    }
                }
                if(!match){
                    result.append(word + " ");
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
        return result.toString();
    }

    /**
     * remove the special symobols from the input text
     * from the filtered text, remove the stop-words
     * @param input to be filtered
     * @return filtered String
     */
    public static String filterString(String input){
        String result="";
        StringTokenizer tokenizer = new StringTokenizer(input);
        String word;
        while (tokenizer.hasMoreTokens())
        {
            word=tokenizer.nextToken();

            boolean b=true;

            //if word is a numbering (1) or (i) or (a) remove it
            if(word.matches("\\((\\w+|\\d+)\\)")){
                b=false;
            }

            //if web url are present
            else if(word.contains("http") || word.contains("https") || word.contains("www")){
                b=false;
            }

            else if(word.matches("(\\()?(\\w(\\)|\\.?|&?))+(,|:|\\)|;|,|.)+?")
                    && !word.matches("(\\S+)?\\d+(\\S+)?(,|:)?")){

                if(word.charAt(word.length()-1)==','
                        ||word.charAt(word.length()-1)=='.'
                        ||word.charAt(word.length()-1)==';'
                        ||word.charAt(word.length()-1)==':'
                        ||word.charAt(word.length()-1)==')'
                        ){

                    word=word.replace(word.charAt(word.length()-1),' ');
                    word=word.trim();
                }
                if(word.charAt(0)=='('){
                    word=word.replace(word.charAt(0),' ');
                    word=word.trim();
                }
                if(word.charAt(word.length()-1)==')'){
                    word=word.replace(word.charAt(word.length()-1),' ');
                    word=word.trim();
                }
                word = word.replace(" ","");

            }
            //if numbers present of the form 233-44
            else if(word.matches("(\\d.?\\w?.?)+(\\,)?")){
                b=false;
            }

            //left-out special characters if any
            else if(word.matches("(.|,|\\)|\\()+")){
                b=false;
            }
            //remove the stop words from the text
            if(b){
                result+=word + " ";
            }
        }
       //after removal of unwanted text, remove the stop-words
       result = handleStopWords(result);
       return result;
    }
    
    /**
     * method to handle the case of state "New York",
     * combine the two words into a single word
     * @param state - name of the state
     * @return String - the combined string
     * */
     public static String combineString(String state) {

         String[] parts = state.split(" ");
         if(parts.length>1){
             state = parts[0].concat(parts[1]);
         }
         return state;
     }
     
     /**
      * checks if the input "state" is a US "state", returns false if not
      * and ends the program
      * @param state
      * @return
      */
     public static boolean checkState(String state){
    	 String statePath = getCurrentPath + "/src/mengg/data" +  "/" + "states.txt";
    	 FileReader reader;
    	 try{
    		 reader = new FileReader(statePath);
    		 BufferedReader brReader = new BufferedReader(reader);
    		 String readLine="";
    		 while((readLine=brReader.readLine())!=null){
    			 if(state.equals(readLine)){
    				 return true;
    			 }
    		 }
    	 }catch(IOException e){
    		 
    		 return false;
    	 }
		return false;
     }
}
