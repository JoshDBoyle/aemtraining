package org.kp.cpc.servlets;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
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
 * Path-based Sling Servlet accepting POST requests.
 * 
 * Each POST request is assumed to contain two parameters:
 * 
 * 		- replicationAgentId: 	the agentId (i.e. xjzxiep0010x) for a replication agent
 * 		- flushAgentId:			the agentId for a flush agent that can be found on the
 * 								publish instance associated with the above replication agent
 * 
 * Given the above two parameters, this servlet makes a GET request over to the publish instance
 * configured for the replication agent with an id specified by replicationAgentId, and passes
 * the flushAgentId for the flush agent we need to reference in making any cache flush requests
 * to a dispatcher.
 * 
 * @author joshua.boyle
 */
@SlingServlet(
	    methods = { "POST" }, 
	    paths = {"/bin/cpc/flushcache" }, 
	    name = "org.kp.cpc.services.FlushCacheServlet")
public class FlushCacheServlet extends SlingAllMethodsServlet {
    static final long serialVersionUID = 1L;

    @Reference
    AgentManager agentMgr;
    
    Logger log = LoggerFactory.getLogger(FlushCacheServlet.class);
    
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	String replicationAgentId = request.getParameter("replicationAgentId");
    	String flushAgentId = request.getParameter("flushAgentId");

    	if(null != replicationAgentId && null != flushAgentId) {
    		HttpClient client = new HttpClient();
    		Agent replicationAgent = agentMgr.getAgents().get(replicationAgentId);
    		String url = replicationAgent.getConfiguration().getTransportURI();
    		
    		url = url.substring(0, url.indexOf("/bin/receive"));

    		if(null != url) {
    			GetMethod get = new GetMethod(url + "/bin/cpc/flushcache");
    			
    			log.debug("CPC: GET request about to be made to publish via the following url: " + url + "/bin/cpc/flushcache");
    			
    			get.setQueryString("id=" + flushAgentId);
	
	            client.executeMethod(get);
	            response.getWriter().println(get.getResponseBodyAsString());
	            get.releaseConnection();
    		}
    	}
    }
}