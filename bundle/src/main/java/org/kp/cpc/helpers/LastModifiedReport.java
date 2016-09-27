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

public class LastModifiedReport {
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
    	params.put("daterange.lowerBound", start.trim().replace(" ", "T"));
		params.put("daterange.lowerOperation", ">=");
		params.put("daterange.upperBound", end.trim().replace(" ", "T"));
		params.put("daterange.upperOperation", "<=");
		params.put("daterange.property", JcrConstants.JCR_CONTENT + "/@cq:lastModified");
		params.put("p.limit", "-1");
		
    	// This is an example of a query for last modified:
		//	    			path=/content
    	//					type=cq:Page
		//					daterange.property=jcr:content/@cq:lastModified
		//					daterange.lowerBound=2014-02-10T00:00
		//					daterange.lowerOperation=>=
		//					daterange.upperBound=2017-02-15T00:00
		//					daterange.upperOperation=<=
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
        String outputResult = "path," + SharedConstants.LAST_MODIFIED_BY_HEADER + "," + SharedConstants.LAST_MODIFIED_HEADER + "\n";

        for(Hit hit : result.getHits()) {
        	Resource res = hit.getResource();
        	ValueMap vm = res.getChild(JcrConstants.JCR_CONTENT).adaptTo(ValueMap.class);
        	
        	outputResult += res.getPath() + ",";
        	
        	if(vm.containsKey(SharedConstants.LAST_MODIFIED_BY_PROPERTY)) {
        		outputResult += vm.get(SharedConstants.LAST_MODIFIED_BY_PROPERTY) + ",";
        	} else {
        		outputResult += SharedConstants.UNKNOWN_MODIFIER;	
        	}
        	
        	if(vm.containsKey(SharedConstants.LAST_MODIFIED_PROPERTY)) {
        		GregorianCalendar gc = (GregorianCalendar)vm.get(SharedConstants.LAST_MODIFIED_PROPERTY);
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
        	
        	if(vm.containsKey(SharedConstants.LAST_MODIFIED_PROPERTY)) {
        		GregorianCalendar gc = (GregorianCalendar)vm.get(SharedConstants.LAST_MODIFIED_PROPERTY);
        		current.put(SharedConstants.LAST_MODIFIED_KEY, gc.getTime().toString());
        	} else {
        		current.put(SharedConstants.LAST_MODIFIED_KEY, SharedConstants.UNKNOWN_MODIFICATION_DATE);
        	}
        	
        	if(vm.containsKey(SharedConstants.LAST_MODIFIED_BY_PROPERTY)) {
        		current.put(SharedConstants.LAST_MODIFIED_BY_KEY, vm.get(SharedConstants.LAST_MODIFIED_BY_PROPERTY));
        	} else {
        		current.put(SharedConstants.LAST_MODIFIED_BY_KEY, "Unknown modifier");	
        	}
        	
        	current.put("path", res.getPath());
        	arr.put(current);
        }
        
        json.put("results", arr);
        
        headers.put("Path");
        headers.put(SharedConstants.LAST_MODIFIED_BY_HEADER);
        headers.put(SharedConstants.LAST_MODIFIED_HEADER);
        
        json.put("totalResults", result.getTotalMatches());
        json.put("headers", headers);

        response.setContentType("application/json");
        response.getWriter().write(json.toString(2));    	
    }
}
