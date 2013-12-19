package mengg.classes.solr;

import org.apache.solr.client.solrj.SolrServerException;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Created with IntelliJ IDEA.
 * User: prasannjit
 * Date: 10/16/13
 * Time: 1:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class GetXMLURLs {

    public GetXMLURLs() { }

    public static ArrayList getXMLUrls(String args)  {

        SolrMatcher sMatcher = new SolrMatcher();
        ArrayList results = new ArrayList();

        //get the results from the SOLR Matcher
        try {
            results = sMatcher.getSolrResults(args);
        } catch(SolrServerException e) {
            e.printStackTrace();
        }

        ArrayList xmlList =null;
        for(Iterator iterator = results.listIterator();iterator.hasNext();) {
            ArrayList array = (ArrayList)iterator.next();
            String path="";
            xmlList=new ArrayList<String>();
            int i=0;
            for(Iterator iterator1 = array.iterator(); iterator1.hasNext();){
                path = (String)iterator1.next();
                xmlList.add(i++, path);
            }
        }

        return xmlList;
    }
}
