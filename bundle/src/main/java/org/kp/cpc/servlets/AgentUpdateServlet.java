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
import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.AgentManager;

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
    	String agentPath = config.getId();
    	Resource agentRes = resolver.resolve(agentPath + "/" + JcrConstants.JCR_CONTENT);
    	JSONObject jsonResponse = new JSONObject();
    	
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
    		}
    	} 
    }
}