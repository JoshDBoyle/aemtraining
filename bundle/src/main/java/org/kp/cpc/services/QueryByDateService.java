package org.kp.cpc.services;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
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
    
    private final String CQ_LAST_MODIFIED = "cq:lastModified";
    private final String CQ_LAST_MODIFIED_BY = "cq:lastModifiedBy";
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
    	JSONObject json = new JSONObject();
    	
    	try {
	    	start = request.getParameter(START);
	    	end = request.getParameter(END);
	    	
	    	params.put("type", NameConstants.NT_PAGE);
	    	params.put("path", ROOT_PATH);
	    	params.put("daterange.property", JcrConstants.JCR_CONTENT + "/@cq:lastModified");

	    	if("" != start) {
	    		start = start.trim().replace(" ", "T");
	    		params.put("daterange.lowerBound", start);
	    		params.put("daterange.lowerOperation", ">=");
	    	}
	    	
	    	if("" != end) {
	    		end = end.trim().replace(" ", "T");
	    		params.put("daterange.upperBound", end);
	    		params.put("daterange.upperOperation", "<=");
	    	}
	    	
	    	params.put("p.limit", "-1");

	    	// This is an example of the query we've built:
			//	    	path=/content
			//			daterange.property=jcr:content/@cq:lastModified
			//			daterange.lowerBound=2014-02-10T00:00
			//			daterange.lowerOperation=>=
			//			daterange.upperBound=2017-02-15T00:00
			//			daterange.upperOperation=<=
	    	//			p.limit=-1 (unbounded)
	    	Query query = qb.createQuery(PredicateGroup.create(params), session);

	        //query.setStart(0);
	        //query.setHitsPerPage(20);
	                   
	        SearchResult result = query.getResult();
	        JSONArray arr = new JSONArray();
	        		
	        for(Hit hit : result.getHits()) {
	        	JSONObject current = new JSONObject();
	        	Resource res = hit.getResource();
	        	ValueMap vm = res.getChild(JcrConstants.JCR_CONTENT).adaptTo(ValueMap.class);
	        	
	        	if(vm.containsKey(CQ_LAST_MODIFIED)) {
	        		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	        		GregorianCalendar gc = (GregorianCalendar)vm.get(CQ_LAST_MODIFIED);
	        		current.put(LAST_MODIFIED, gc.getTime().toString());
	        	} else {
	        		current.put(LAST_MODIFIED, "Unknown date");
	        	}
	        	
	        	if(vm.containsKey(CQ_LAST_MODIFIED_BY)) {
	        		current.put(LAST_MODIFIED_BY, vm.get(CQ_LAST_MODIFIED_BY));
	        	} else {
	        		current.put(LAST_MODIFIED, "Unknown modifier");	
	        	}
	        	
	        	current.put("path", res.getPath());
	        	arr.put(current);
	        }
	        
	        json.put("results", arr);
	        json.put("totalResults", result.getTotalMatches());

	        response.setContentType("application/json");
	        response.getWriter().write(json.toString(2));
    	} catch(Exception e) {
    		log.error("Generic exception caught in QueryByDateService trying to retrieve results by date range");
    		response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // If you do not set your own HTML Response content, the OOTB HATEOS Response is used
            response.getWriter().write("An error occured in QueryByDateService.doGet.");
    	}	
    }
}