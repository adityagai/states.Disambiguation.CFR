package mengg.classes.solr;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: prasannjit
 * Date: 10/16/13
 * Time: 1:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class SolrMatcher {

    public SolrMatcher() { }

    public ArrayList getSolrResults(String queryString) throws SolrServerException {

        HttpSolrServer solr = new HttpSolrServer("http://gulo.law.cornell.edu:8983/solr");

        ArrayList results = new ArrayList();
        ModifiableSolrParams params = new ModifiableSolrParams();

        params.set("defType", new String[] { "edismax" });
        params.set("start", new String[] { "0" });
        params.set("rows", new String[] { "200" });
        params.set("fq", new String[] { "(cfrtitlenumber:7)" });
        params.set("q", new String[] { (new StringBuilder("\"")).append(queryString).append("\"").toString() });

        //Create the query and get the response
        QueryResponse response = solr.query(params);

        //Generate the document list from the response
        SolrDocumentList list = response.getResults();

        ArrayList pathResults = new ArrayList();

        //Add the paths to pathResults
        for(int j = 0; j < list.size(); j++) {
            String path = ((SolrDocument)list.get(j)).getFieldValue("path").toString();
            pathResults.add(path);
        }

        //add to the results
        results.add(pathResults);

        //return the list of paths
        return results;
    }
}

