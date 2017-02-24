package org.kp.cpc.flush.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
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

/**
 * Path-based Sling Servlet that accepts GET requests and attempts to flush the cache
 * for the dispatcher configured within the flush agent specified by the id parameter
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
    	PrintWriter writer;
    	String agentId = request.getParameter("id");

    	try { 
            if(null != agentId) {
            	Agent agent = agentMgr.getAgents().get(agentId);
            	
            	if(null != agent) {
            		HttpClient client = new HttpClient();
            		PostMethod post = new PostMethod(agent.getConfiguration().getTransportURI());

            		/**
            		 * Here's a sample curl request we're attempting to emulate here:
            		 * 
            		 * 		curl -v \
							-H "CQ-Action: DELETE" \
							-H "CQ-Handle:/" \
							-H "Content-Length: 0" \
							-H "Content-Type: application/octet-stream" \
							http://localhost:80/dispatcher/invalidate.cache;
            		 */
            		log.debug("CPC:  We're about to make our POST request for cache deletion to the dispatcher at :" + agent.getConfiguration().getTransportURI());

            		post.setRequestHeader("CQ-Action", "DELETE");
            		post.setRequestHeader("CQ-Handle", "/");
            		post.setRequestHeader("Content-Length", "0");
            		post.setRequestHeader("Content-Type", "application/octet-stream");

                    client.executeMethod(post);

                    log.debug("CPC: POST has been made and here's what we have back from the dispatcher: " + post.getResponseBodyAsString());

                    post.releaseConnection();

                    response.setContentType("text/plain; charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_OK);
                    writer = response.getWriter();
                    writer.println("Cache deletion successful");
            	}
            }            
        } catch(IOException ie) {
        	response.setContentType("text/plain; charset=UTF-8");
        	response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        	writer = response.getWriter();
        	writer.println("Cache deletion failed with the following response: " + ie.getMessage());
            log.error("IOException caught in FlushCacheServlet: " + ie.getMessage());
        } catch(Exception e) {
        	response.setContentType("text/plain; charset=UTF-8");
        	response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        	writer = response.getWriter();
        	writer.println("Cache deletion failed with the following response: " + e.getMessage());
        	log.error("Generic Exception caught in FlushCacheServlet: " + e.getMessage());
        }
    }
}