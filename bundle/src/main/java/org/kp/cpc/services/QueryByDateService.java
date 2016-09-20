package org.kp.cpc.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.NameConstants;

@SlingServlet(
	    methods = { "GET" }, 
	    paths = {"/bin/cpc/querybydate" }, 
	    name = "org.kp.cpc.services.QueryByDateService")
public class QueryByDateService extends SlingAllMethodsServlet {
    static final long serialVersionUID = 1L;
    
    private final String ROOT_PATH = "/content";
    private final String START = "startdate";
    private final String END = "enddate";
    
    private final String LAST_MODIFIED = "lastModified";
    private final String LAST_MODIFIED_BY = "lastModifiedBy";
    
    @Reference
    QueryBuilder qb;
    
    Logger log = LoggerFactory.getLogger(QueryByDateService.class);
    
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	ResourceResolver resolver = request.getResourceResolver();
    	Session session = resolver.adaptTo(Session.class);
    	String start = "";
    	String end = "";
    	Map<String, String> params = new HashMap<String, String>();
    	PredicateGroup pGroup = new PredicateGroup();
    	
    	try {
	    	start = request.getParameter(START);
	    	end = request.getParameter(END);
	    	
	    	params.put("type", NameConstants.NT_PAGE);
	    	params.put("path", "/content");
	    	params.put("daterange.property", JcrConstants.JCR_CONTENT + "/" + NameConstants.PN_LAST_MOD);

	    	if("" != start) {
	    		params.put("daterange.lowerBound", start);
	    		params.put("daterange.lowerOperation", ">=");
	    	}
	    	
	    	if("" != end) {
	    		params.put("daterange.upperBound", end);
	    		params.put("daterange.upperOperation", "<=");
	    	}

	    	Query query = qb.createQuery(PredicateGroup.create(params), session);

	        query.setStart(0);
	        query.setHitsPerPage(20);
	                   
	        SearchResult result = query.getResult();
	      
	        // paging metadata
	        int hitsPerPage = result.getHits().size(); // 20 (set above) or lower
	        long totalMatches = result.getTotalMatches();
	        long offset = result.getStartIndex();
	        long numberOfPages = totalMatches / 20;
	        
	        log.error("YO JOSH WE GOT: " + totalMatches + " total matches for that query!");
	        
	        JSONObject json = new JSONObject();
	        JSONArray arr = new JSONArray();
	        		
	        for(Hit hit : result.getHits()) {
	        	JSONObject current = new JSONObject();
	        	ValueMap vm = hit.getResource().adaptTo(ValueMap.class);
	        	
	        	if(vm.containsKey(NameConstants.PN_LAST_MOD))
	        		current.put(LAST_MODIFIED, vm.get(NameConstants.PN_LAST_MOD));
	        	else
	        		current.put(LAST_MODIFIED, "Unknown date");
	        	
	        	if(vm.containsKey(NameConstants.PN_LAST_MOD_BY))
	        		current.put(LAST_MODIFIED_BY, vm.get(NameConstants.PN_LAST_MOD_BY));
	        	else
	        		current.put(LAST_MODIFIED, "Unknown modifier");	
	        	
	        	arr.put(current);
	        }
	        
	        json.put("results", arr);
	        json.put("totalResults", totalMatches);
	    	
    	} catch(Exception e) {
    		log.error("Generic exception caught in QueryByDateService trying to retrieve results by date range");
    	}
    }
}