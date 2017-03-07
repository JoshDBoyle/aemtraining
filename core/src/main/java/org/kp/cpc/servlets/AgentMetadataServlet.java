package org.kp.cpc.servlets;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentManager;
import com.day.cq.replication.ReplicationQueue;
import com.day.cq.replication.ReplicationQueue.Entry;

/**
 * Path-based Sling Servlet that puts a single Agent into "standby" mode
 * 
 * @author joshua.boyle
 */
@SlingServlet(
	    methods = { "GET" }, 
	    paths = {"/bin/cpc/agentmetadata" }, 
	    name = "org.kp.cpc.servlets.AgentMetadataServlet")
public class AgentMetadataServlet extends SlingAllMethodsServlet {
    static final long serialVersionUID = 1L;

    @Reference
    AgentManager agentMgr;
    
    Logger log = LoggerFactory.getLogger(AgentMetadataServlet.class);
    
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	String agentId = request.getParameter("agentId");
    	JSONObject metadata = new JSONObject();
    	
    	response.setContentType("application/json");
    	if(null != agentId) {
    		Agent agent = agentMgr.getAgents().get(agentId);
    		ResourceResolver resolver = request.getResourceResolver();
    		ReplicationQueue queue = agent.getQueue();
    		ValueMap agentVM = resolver.resolve("/etc/replication/agents.author/" + agentId +"/" + JcrConstants.JCR_CONTENT).adaptTo(ValueMap.class);
    		
    		try {
	    		metadata.put("agentId", agentId);
	    		metadata.put("standby", agentVM.containsKey("standby"));
	    		metadata.put("enabled", agentVM.containsKey("enabled"));
	    		
	    		if(null != queue) {
	    			metadata.put("blocked", queue.getStatus().getProcessingSince() <= 0);
	    			
		    		JSONArray queueArr = new JSONArray();
		    		for(Entry entry : queue.entries()) {
		    			JSONObject queueEntry = new JSONObject();
		    			queueEntry.put("path", entry.getAction().getPath());
		    			queueEntry.put("type", entry.getAction().getType());
		    			queueArr.put(queueEntry);
		    		}

		    		metadata.put("queue", queueArr);
	    		} else {
	    			metadata.put("blocked", false);
	    		}

	    		response.getWriter().write(metadata.toString(2)); 
    		} catch(JSONException e) {
    			log.error("JSONException caught in AgentMetadataServlet while trying to build a JSONObject of metadata about Agent " + agentId);
    		}
    	}
    }
}