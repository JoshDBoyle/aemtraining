package org.kp.cpc.flush.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentConfig;
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
    	JSONObject jsonResponse = new JSONObject();
    	JSONArray jsonArr = new JSONArray();
    	Map<String, Agent> agents = agentMgr.getAgents();

    	agents.forEach((id, agent)->{
    		AgentConfig config = agent.getConfiguration();
    		JSONObject current = new JSONObject();
    		try {
    			current.put("title", config.getName());
    			current.put("id", config.getId());
    			current.put("agentId", config.getAgentId());
    			current.put("paused", agent.getQueue().isPaused());
    			jsonArr.put(current);
    		} catch(JSONException e) {
    			log.error("JSONException caught in GetFlushAgentsServlet.doGet while attempting to build a JSONObject for an Agent.");
    		} catch(Exception e) {
    			log.error("Generic Exception caught in GetFlushAgentsServlet.doGet while attempting to build a JSONObject for an Agent.");
    		}
    	});

    	try {
    		jsonResponse.put("agents", jsonArr);
		} catch(JSONException e) {
			log.error("JSONException caught in GetFlushAgentsServlet.doGet while attempting to add the current agent to the JSONArray.");
		} catch(Exception e) {
			log.error("Generic Exception caught in GetFlushAgentsServlet.doGet while attempting to add the current agent to the JSONArray.");
		}
    	
    	response.setContentType("application/json");
    
    	PrintWriter out = response.getWriter();
    	out.print(jsonResponse);
    	out.flush();
    }
}