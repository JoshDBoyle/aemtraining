package org.kp.cpc.flush.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.AgentManager;
import com.day.cq.replication.ReplicationActionType;

/**
 * Path-based Sling Servlet that accepts GET requests to retrieve metadata about any available flush agents
 * 
 * @author joshua.boyle
 */
@SlingServlet(
	    methods = { "GET" }, 
	    paths = {"/bin/cpc/flushcache" }, 
	    name = "org.kp.cpc.flush.servlets.FlushCacheServlet")
public class FlushCacheServlet extends SlingAllMethodsServlet {
    static final long serialVersionUID = 1L;

    @Reference
    AgentManager agentMgr;
    
    Logger log = LoggerFactory.getLogger(FlushCacheServlet.class);
    
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	
    	PrintWriter writer = response.getWriter();
    	String agentId = request.getParameter("id");
    	
    	try { 
            if(null != agentId) {
            	Agent agent = agentMgr.getAgents().get(agentId);
            	
            	if(null != agent) {
            		HttpClient client = new HttpClient();
            		PostMethod post = new PostMethod(agent.getConfiguration().getTransportURI());
            		log.error("JOSH I'M ABOUT TO MAKE MY POST REQUEST");
            		post.setRequestHeader("CQ-Action", "Activate");
            		post.setRequestHeader("CQ-Handle", "/content/kporg/**");

                    client.executeMethod(post);
                    post.releaseConnection();

                    log.error("JOSH HERE'S WHAT THE DISPATCHER HAS TO SAY ABOUT THAT INVALIDATION: " + post.getResponseBodyAsString());
                    writer.print("The cache for the " + agentId + " dispatcher was successfully invalidated");
            	}
            }            
        } catch(Exception e){
        	writer.print("The cache for the " + agentId + " dispatcher was unable to be invalidated due to an error");
            log.error("Generic Exception caught in FlushCacheServlet: " + e.getMessage());
        }
    }
}