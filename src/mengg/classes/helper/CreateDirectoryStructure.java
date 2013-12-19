package mengg.classes.helper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: prasannjit
 * Date: 11/10/13
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * @author Prasannjit
Program that creates a folder "states"
containing the folder "names of all US states"
* */
public class CreateDirectoryStructure {

    public static String getCurrentPath = new java.io.File("").getAbsolutePath();

    /**
    * @param state
    * The input state for which the folder is to be created
    *
    * @return String state
    * return the input state back
    * */
    public static String createStructureInputState(String state){

        String statePath = getCurrentPath + "/src/mengg/data/input/" + "states";

        File parentFile = new File(statePath);

        //make the parent folder "states" if non-existant
        if(!parentFile.exists()){
            if(parentFile.mkdir()){
                //do nothing
            }
        }

        String path=statePath + "/" + state;
        File stateFile = new File(path);

        if(!stateFile.exists()){

            //if folder doesn't exist then create it
            boolean result = stateFile.mkdir();
            if(result){
                System.out.println("folder " + (state) + " created");
            }
        }

        return state;

    }

    /**
    * list of states for which the folder is to be created
    * in the "states" folder of the "data" folder
    *
    * For example if state is Michigan, it is created at /src/mengg/data/states/Michigan
    * @param stateList
    *
    * @return ArrayList<String>
    *  return back the list of States read in an ArrayList
    * */
    public static ArrayList<String> createStructureList(List<String> stateList) {

        String statePath = getCurrentPath + "/src/mengg/data/" + "states";
        File parentFile = new File(statePath);

        //make the parent folder "states" if non-existant
        if(!parentFile.exists()){
            if(parentFile.mkdir()){
                //do nothing
            }
        }

        //create the state folders
        String path;
        ArrayList<String> list = new ArrayList<String>();
        if(stateList!=null){

            //handle the case for "New York"
             for(String s: stateList){
                 list.add(s.trim());
                 path = statePath + "/" + s.trim();
                 File stateFile = new File(path);
                 if(!stateFile.exists()){

                     //if folder doesn't exist then create it
                     boolean result = stateFile.mkdir();
                     if(result){
                         System.out.println("folder " + s.trim() + " created");
                     }
                 }
             }
        }
        return list;
    }

    
}
