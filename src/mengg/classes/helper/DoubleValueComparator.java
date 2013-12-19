package mengg.classes.helper;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: prasannjit
 * Date: 11/6/13
 * Time: 12:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class DoubleValueComparator implements Comparator<String> {
    HashMap<String,Double> map;

    public DoubleValueComparator(HashMap<String, Double> map) {
        this.map=map;


    }
    @Override
    public int compare(String s, String s2) {
        if(map.get(s) > map.get(s2))
            return -1;
        else if(map.get(s)==map.get(s2)){
            if(s.compareTo(s2)>0)
                return 1;
            else
                return -1;
        }
        return 1;
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
