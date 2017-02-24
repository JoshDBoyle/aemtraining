package org.kp.cpc.helpers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.GregorianCalendar;
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
 * Generates a json or csv report about content that has been activated.
 * Called from QueryByDateServlet.
 * 
 * @author joshua.boyle
 */
public class LockedReport {
	public static void buildReport(
			SlingHttpServletResponse response, 
			Session session,
			QueryBuilder qb, 
			String start, 
			String end, 
			String csv) {
		
		Map<String, String> params = new HashMap<String, String>();
		
		params.put("type", NameConstants.NT_PAGE);
    	params.put("path", SharedConstants.ROOT_PATH);

    	if(null != start && !start.equals("")) {
	    	params.put("daterange.lowerBound", start.trim().replace(" ", "T"));
			params.put("daterange.lowerOperation", ">=");
    	}

    	if(null != end && !end.equals("")) {
    		params.put("daterange.upperBound", end.trim().replace(" ", "T"));
    		params.put("daterange.upperOperation", "<=");
    	}

		params.put("daterange.property", JcrConstants.JCR_CONTENT + "/@cq:lastReplicated");
		params.put("property", "jcr:content/@cq:lastReplicationAction");
		params.put("property.value", "Activate");
		params.put("p.limit", "-1");
		
		// This is an example of a query for last replicated where the replication action was Activate
		//	    			path=/content
		//	    			type=cq:Page
		//	    			daterange.property=jcr:content/@cq:lastReplicated
		//	    			daterange.lowerBound=2013-02-10T00:00:00
		//	    			daterange.lowerOperation=>=
		//	    			daterange.upperBound=2017-02-10T00:00:00
		//	    			daterange.upperOperation=<=
		//	    			property=jcr:content/@cq:lastReplicationAction
		//	    			property.value=Deactivate
    	//					p.limit=-1 (unbounded)
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
        String outputResult = "path," + SharedConstants.LAST_ACTIVATED_BY_HEADER + "," + SharedConstants.LAST_ACTIVATED_HEADER + "\n";

        for(Hit hit : result.getHits()) {
        	Resource res = hit.getResource();
        	ValueMap vm = res.getChild(JcrConstants.JCR_CONTENT).adaptTo(ValueMap.class);
        	
        	outputResult += res.getPath() + ",";
        	
        	if(vm.containsKey(SharedConstants.LAST_ACTIVATED_BY_PROPERTY)) {
        		outputResult += vm.get(SharedConstants.LAST_ACTIVATED_BY_PROPERTY) + ",";
        	} else {
        		outputResult += SharedConstants.UNKNOWN_ACTIVATOR;	
        	}
        	
        	if(vm.containsKey(SharedConstants.LAST_ACTIVATED_PROPERTY)) {
        		GregorianCalendar gc = (GregorianCalendar)vm.get(SharedConstants.LAST_ACTIVATED_PROPERTY);
        		outputResult += gc.getTime().toString() + "\n";
        	} else {
        		outputResult += SharedConstants.UNKNOWN_ACTIVATION_DATE + "\n";
        	}
        }

        response.setHeader("Content-type", "text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"activated-content-by-date.csv\"");
        
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
        	
        	if(vm.containsKey(SharedConstants.LAST_ACTIVATED_PROPERTY)) {
        		GregorianCalendar gc = (GregorianCalendar)vm.get(SharedConstants.LAST_ACTIVATED_PROPERTY);
        		current.put(SharedConstants.LAST_ACTIVATED_KEY, gc.getTime().toString());
        	} else {
        		current.put(SharedConstants.LAST_ACTIVATED_KEY, SharedConstants.UNKNOWN_ACTIVATION_DATE);
        	}
        	
        	if(vm.containsKey(SharedConstants.LAST_ACTIVATED_BY_PROPERTY)) {
        		current.put(SharedConstants.LAST_ACTIVATED_BY_KEY, vm.get(SharedConstants.LAST_ACTIVATED_BY_PROPERTY));
        	} else {
        		current.put(SharedConstants.LAST_ACTIVATED_BY_KEY, SharedConstants.UNKNOWN_ACTIVATOR);	
        	}
        	
        	current.put("path", res.getPath());
        	arr.put(current);
        }
        
        json.put("results", arr);
        
        headers.put("Path");
        headers.put(SharedConstants.LAST_ACTIVATED_BY_HEADER);
        headers.put(SharedConstants.LAST_ACTIVATED_HEADER);
        
        json.put("totalResults", result.getTotalMatches());
        json.put("headers", headers);

        response.setContentType("application/json");
        response.getWriter().write(json.toString(2));    	
    }
}
