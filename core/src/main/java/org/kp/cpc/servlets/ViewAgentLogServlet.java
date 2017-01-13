package org.kp.cpc.servlets;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentManager;

/**
 * Path-based Sling Servlet that replicates a number of paths
 * 
 * @author joshua.boyle
 */
@SlingServlet(
	    methods = { "GET" }, 
	    paths = {"/bin/cpc/activateselected" }, 
	    name = "org.kp.cpc.services.ActivateSelectedServlet")
public class ViewAgentLogServlet extends SlingAllMethodsServlet {
    static final long serialVersionUID = 1L;

    @Reference
    AgentManager agentMgr;
    
    Logger log = LoggerFactory.getLogger(ViewAgentLogServlet.class);
    
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	String agentId = request.getParameter("id");
    	
    	if(null != agentId) {
    		Agent agent = agentMgr.getAgents().get(agentId);
    		
    		if(null != agent) {
    			Collection<String> lines = agent.getLog().getLines();

    			// Build the response for display client-side
    			if(null != lines) {
		    		while(lines.iterator().hasNext()) {
		    			String line = lines.iterator().next();
		                
		    		}
    			}
    		}
    	}
    }
}