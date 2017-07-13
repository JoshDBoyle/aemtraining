package org.kp.cpc.helpers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.NameConstants;

/**
 * Generates a json or csv report about content that locked.
 * Called from BuildReportServlet.
 * 
 * @author joshua.boyle
 */
public class LockedReport {
	public static void buildReport(
			SlingHttpServletResponse response, 
			Session session,
			QueryBuilder qb,
			String csv) {
		
		Map<String, String> params = new HashMap<String, String>();
		
		params.put("type", NameConstants.NT_PAGE);
    	params.put("path", SharedConstants.ROOT_PATH);
		params.put("property", "jcr:content/@jcr:lockOwner");
		params.put("property.operation", "exists");
		params.put("p.limit", "-1");
		
		// This is an example of a query for locked paths
		//	    			path=/content
		//	    			type=cq:Page
		//	    			property=jcr:content/@jcr:lockOwner
		//	    			property.operation=exists
    	//					p.limit=500 (unbounded)
		Query query = qb.createQuery(PredicateGroup.create(params), session);

        SearchResult result = query.getResult();
        
        try {
	        if(csv.equals("true")) {
	        	buildFile(response, result);
	        } else {
	        	buildJson(response, result);
	        }
        } catch(IOException e) {
        	
        } catch(RepositoryException e) {
        	
        } catch(JSONException e) {
        	
        }
	}
	
    private static void buildFile(SlingHttpServletResponse response, SearchResult result) throws RepositoryException, IOException {
        OutputStream outputStream = response.getOutputStream();
        String outputResult = "path," + SharedConstants.LOCKED_BY_HEADER + "\n";

        for(Hit hit : result.getHits()) {
        	Resource res = hit.getResource();
        	ValueMap vm = res.getChild(JcrConstants.JCR_CONTENT).adaptTo(ValueMap.class);
        	
        	outputResult += res.getPath() + ",";
        	
        	if(vm.containsKey(SharedConstants.LOCKED_BY_PROPERTY)) {
        		outputResult += vm.get(SharedConstants.LOCKED_BY_PROPERTY) + ",";
        	} else {
        		outputResult += SharedConstants.UNKNOWN_LOCK_OWNER;	
        	}
        }

        response.setHeader("Content-type", "text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"locked-content.csv\"");
        
        outputStream.write(outputResult.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    private static void buildJson(SlingHttpServletResponse response, SearchResult result) throws RepositoryException, JSONException, IOException {
    	JSONObject json = new JSONObject();
    	JSONArray headers = new JSONArray();
        JSONArray arr = new JSONArray();
		
        for(Hit hit : result.getHits()) {
        	JSONObject current = new JSONObject();
        	Resource res = hit.getResource();
        	ValueMap vm = res.getChild(JcrConstants.JCR_CONTENT).adaptTo(ValueMap.class);
        	
        	if(vm.containsKey(SharedConstants.LOCKED_BY_PROPERTY)) {
        		current.put(SharedConstants.LOCKED_BY_KEY, vm.get(SharedConstants.LOCKED_BY_PROPERTY));
        	} else {
        		current.put(SharedConstants.LOCKED_BY_KEY, SharedConstants.UNKNOWN_LOCK_OWNER);
        	}
        	
        	current.put("path", res.getPath());
        	arr.put(current);
        }
        
        json.put("results", arr);
        
        headers.put("Path");
        headers.put(SharedConstants.LOCKED_BY_HEADER);
        
        json.put("totalResults", result.getTotalMatches());
        json.put("headers", headers);

        response.setContentType("application/json");
        response.getWriter().write(json.toString(2));    	
    }
}
