package org.kp.cpc.flush.servlets;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentManager;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
 
@SlingServlet(
	    methods = { "POST" }, 
	    paths = {"/bin/cpc/flushcache" }, 
	    name = "org.kp.cpc.flush.servlets.FlushCacheServlet")
public class FlushCacheServlet extends SlingSafeMethodsServlet {

	private static final long serialVersionUID = 1L;
 
    private Logger logger = LoggerFactory.getLogger(this.getClass());
 
    @Reference
	private AgentManager agentMgr;
    
    public void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        try {
            String agentId = request.getParameter("id");

            if(null != agentId) {
            	Agent flushAgent = agentMgr.getAgents().get(agentId);
	            HttpClient client = new HttpClient();
	 
	            PostMethod post = new PostMethod(flushAgent.getConfiguration().getTransportURI());
	            post.setRequestHeader("CQ-Action", "Activate");
	            post.setRequestHeader("CQ-Handle","/");
	            post.setRequestHeader("Content-length", "0");

	            client.executeMethod(post);
	            post.releaseConnection();

	            logger.info("Cache invalidation performed for the following dispatcher: " + flushAgent.getConfiguration().getTransportURI());
            }
        } catch(Exception e){
            logger.error("Generic Exception caught while attempting to flush dispatcher cache: " + e.getMessage());
        }
    }
}