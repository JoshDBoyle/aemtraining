package org.kp.cpc.flush.servlets;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.AgentManager;

/**
 * Path-based Sling Servlet that accepts GET requests to retrieve metadata about any available flush agents
 * 
 * @author joshua.boyle
 */
@SlingServlet(
	    methods = { "POST" }, 
	    paths = {"/bin/cpc/getflushagents" }, 
	    name = "org.kp.cpc.flush.servlets.GetFlushAgentsServlet")
public class GetFlushAgentsServlet extends SlingAllMethodsServlet {
    static final long serialVersionUID = 1L;

    @Reference
    AgentManager agentMgr;
    
    Logger log = LoggerFactory.getLogger(GetFlushAgentsServlet.class);
    
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	//TODO: Retrieve any flush agents on this instance and build a JSONArray of the form:
    	//	{
    	//		agents: [
    	//			{
    	//				title: AgentConfig.getName(),
    	//				id: AgentConfig.getId(),
    	//				agentId: AgentConfig.getAgentId(),
    	//				paused: Agent.getQueue().isPaused()
    	//			},
    	//			{
    	//				title: AgentConfig.getName(),
    	//				id: AgentConfig.getId(),
    	//				agentId: AgentConfig.getAgentId(),
    	//				paused: Agent.getQueue().isPaused()
    	//			}
    	//	]}

    }
}