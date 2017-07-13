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

/**
 * Path-based Sling Servlet that puts a single Agent into "standby" mode
 * 
 * @author joshua.boyle
 */
@SlingServlet(
	    methods = { "POST" }, 
	    paths = {"/bin/cpc/updateagent" }, 
	    name = "org.kp.cpc.servlets.AgentUpdateServlet")
public class AgentUpdateServlet extends SlingAllMethodsServlet {
    static final long serialVersionUID = 1L;

    @Reference
    AgentManager agentMgr;
    
    Logger log = LoggerFactory.getLogger(AgentUpdateServlet.class);
    
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	ResourceResolver resolver = request.getResourceResolver();
    	Session session = resolver.adaptTo(Session.class);
    	String agentId = request.getParameter("id");
    	String type = request.getParameter("type");
    	String value = request.getParameter("value");
    	
    	AgentConfig config = agentMgr.getAgents().get(agentId).getConfiguration();
    	String agentPath = config.getId();
    	Resource agentRes = resolver.resolve(agentPath + "/" + JcrConstants.JCR_CONTENT);
    	JSONObject jsonResponse = new JSONObject();

        /**
    	 * AEM OOTB allows for enabling/disabling an Agent or pausing/unpausing an Agent's Queue.  However, our use case is that we want to be able
    	 * to "pause" an Agent indefinitely such that it doesn't replicate anything but so that content can still be queued up for replication.  If
    	 * you disable an Agent, nothing queues up as the Agent doesn't receive replication requests.  If you pause an Agent's Queue, content DOES
    	 * queue up but eventually Sling will drop the thread and the paused/unpaused state is lost.  Further, if an Agent's Queue hasn't received
    	 * an event in a while, pausing/unpausing the Queue is not possible because no thread exists to handle it.
    	 * 
    	 * So to get around these limitations, we're going to swap out the transportURI of the Agent we want to "pause" instead.  This will cause the
    	 * Agent to stay "active" such that it receives replication requests but they will not be able to be replicated and thus they'll just stay
    	 * in a blocked queue.  I'm referring to this mode as "standby".
    	 */
    	if(null != agentRes) {
    		ModifiableValueMap mvm = agentRes.adaptTo(ModifiableValueMap.class);
    		boolean isInStandby = mvm.containsKey("standby");
    		
    		if(type.equals("standby")) {
	    		// IF we want to put this Agent in standby
	    		// THEN copy the Agent's real transportURI to a property named "standby"
	    		// ELSE set the Agent's transportURI back to it's proper value and remove the standby property
	    		if(value.equals("true") && !isInStandby) {
	    			String transportUri = config.getTransportURI();
	    			mvm.put("standby", transportUri);
	    			mvm.put("transportUri", "standby");
	    		} else {
    				mvm.put("transportUri", mvm.get("standby"));
    				mvm.remove("standby");
	    		}
    		} else if(type.equals("enabled")) {
    			/**
    			 * Enabling an Agent in AEM works via an "enabled" property on the Agent's jcr:content node.
    			 * To disable, you can either remove that property or set it to a non-true value.  OOTB, AEM
    			 * removes the property so we'll do the same for consistency.
    			 */
	    		if(value.equals("true")) {
	    			mvm.put("enabled", "true");
	    		} else {
	    			mvm.remove("enabled");
	    		}
    		}

    		try {
    			session.save(); 
    			jsonResponse.put("agentId", agentId);
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
}