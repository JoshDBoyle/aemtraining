package org.kp.cpc.flush.servlets;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentManager;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
 
@Component(metatype=true)
@Service
public class FlushCacheServlet extends SlingSafeMethodsServlet {
 
    @Property(value="/bin/flushcache")
    static final String SERVLET_PATH="sling.servlet.paths";
 
    private Logger logger = LoggerFactory.getLogger(this.getClass());
 
    @Reference
	private AgentManager agentMgr;
    
    public void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        try {
            String agentPath = request.getParameter("agentPath");
            Resource temp = request.getResourceResolver().resolve(agentPath);
            
/*            if(null != temp) {
            	Agent flushAgent = temp.adaptTo(Agent.class);
	            HttpClient client = new HttpClient();
	 
	            PostMethod post = new PostMethod(flushAgent.getConfiguration().getTransportURI());
	            post.setRequestHeader("CQ-Action", "Activate");
	            post.setRequestHeader("CQ-Handle","/");
	             
	            StringRequestEntity body = new StringRequestEntity(page,null,null);
	            post.setRequestEntity(body);
	            post.setRequestHeader("Content-length", String.valueOf(body.getContentLength()));
	            client.executeMethod(post);
	            post.releaseConnection();
	            //log the results
	            logger.info("result: " + post.getResponseBodyAsString());
            }*/
        }catch(Exception e){
            logger.error("Flushcache servlet exception: " + e.getMessage());
        }
    }
}