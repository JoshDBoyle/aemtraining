package org.kp.cpc.servlets;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.AgentManager;
import com.day.cq.replication.ReplicationQueue;

/**
 * Path-based Sling Servlet that enables or disables a single Agent based on a RequestParameter
 * 
 * @author joshua.boyle
 */
@SlingServlet(
	    methods = { "POST" }, 
	    paths = {"/bin/cpc/updateagent" }, 
	    name = "org.kp.cpc.services.AgentUpdateService")
public class AgentUpdateServlet extends SlingAllMethodsServlet {
    static final long serialVersionUID = 1L;

    @Reference
    AgentManager agentMgr;
    
    Logger log = LoggerFactory.getLogger(AgentUpdateServlet.class);
    
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	ResourceResolver resolver = request.getResourceResolver();
    	Session session = resolver.adaptTo(Session.class);
    	String agentId = request.getParameter("id");
    	String enabled = request.getParameter("enabled");
    	
    	AgentConfig config = agentMgr.getAgents().get(agentId).getConfiguration();
    	
    	if(null != config) {
    		log.error("CPC: We have an AgentConfig fromm the agentId that was specified so we're good");
    	}

    	String agentPath = config.getId();
    	Resource agentRes = resolver.resolve(agentPath + "/" + JcrConstants.JCR_CONTENT);
    	JSONObject jsonResponse = new JSONObject();
        Agent agent = agentId == null ? null : agentMgr.getAgents().get(agentId);
        ReplicationQueue queue = agent.getQueue();

        /**
    	 * The enabled state of an agent is determined by the "enabled" property both being present
    	 * on the jcr:content node for the agent as well as this property having a value of "true".
    	 * If this property either has a value of "false" or is completely absent, the agent is
    	 * considered as disabled by AEM.  Since AEM prefers to completely remove this property
    	 * when disabling an agent, we'll do the same for consistency.
    	 * 
    	 * It should also be noted that a disabled agent doesn't queue up content and although you can
    	 * pause an agent's queue instead of disabling, pausing the queue requires that there actually
    	 * BE a queue of content.  A use case in the CPC is that we want to be able to put an agent in
    	 * a state that is neither paused nor disabled where content still queues up but doesn't activate.
    	 * For CPC 1.0's release, this feature will not be completed so we're just going with disabling
    	 * and enabling of agents entirely as it's cleanest.
    	 */
    	if(null != agentRes) {
    		ModifiableValueMap mvm = agentRes.adaptTo(ModifiableValueMap.class);
    		if(enabled.equals("true"))
    			mvm.put("enabled", enabled);
    		else
    			mvm.remove("enabled");
    		
    		try {
    			session.save();
    			jsonResponse.put("agentId", agentId);
    			jsonResponse.put("enabled", enabled);
    			response.setContentType("application/json");
    	        response.getWriter().write(jsonResponse.toString(2)); 
    		} catch(RepositoryException rex) {
    			log.error("Problem saving session in AgentUpdateService.doPost");
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Problem saving session in AgentUpdateService.doPost.");
    		} catch(JSONException jex) {
    			log.error("Problem setting JSON response in AgentUpdateService.doPost");
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Problem setting JSON response in AgentUpdateService.doPost.");
    		} finally {
    			session.logout();
    		}
    	} 
    }

    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	doPost(request, response);
    }
}