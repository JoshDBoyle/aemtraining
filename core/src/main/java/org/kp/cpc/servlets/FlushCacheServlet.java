package org.kp.cpc.servlets;

import java.io.IOException;

import javax.jcr.Session;
import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentManager;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;

/**
 * Path-based Sling Servlet that replicates the paths specified by a comma-delimited request parameter
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
    			log.error("JOSH HERE'S THE GET REQUEST URL WE'RE ABOUT TO MAKE FOR CACHE FLUSH: " + url);
    			get.setQueryString("id=" + flushAgentId);
	
	            client.executeMethod(get);
	            get.releaseConnection();
    		}
    	}
    }
}