package org.kp.cpc.servlets;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMException;

/**
 * Path-based Sling Servlet that unlocks the paths specified by a comma-delimited request parameter
 * 
 * @author joshua.boyle
 */
@SlingServlet(
	    methods = { "POST" }, 
	    paths = {"/bin/cpc/unlockselected" }, 
	    name = "org.kp.cpc.servlets.UnlockSelectedServlet")
public class UnlockSelectedServlet extends SlingAllMethodsServlet {
    static final long serialVersionUID = 1L;
    
    Logger log = LoggerFactory.getLogger(UnlockSelectedServlet.class);
    
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	String[] paths = request.getParameter("paths").split(",");
    	ResourceResolver resolver = request.getResourceResolver();
    	boolean success = true;

    	if(null != paths && paths.length > 0) {
    		for(String path : paths) {
				Resource resource = resolver.resolve(path);
				if(null != resource) {
					Page page = resource.adaptTo(Page.class);
					
					if(null != page) {
						try {
							page.unlock();
						} catch(WCMException e) {
							success = false;
							log.error("WCMException caught in UnlockSelectedServlet while trying to unlock a Page at path: " + page.getPath());
						}
					}
				}
    		}
    	}

		if(success) {
			response.getWriter().write("The selected paths were successfully unlocked");
		} else {
			response.getWriter().write("Some or all of the paths selected were unable to be unlocked");
		}
    }
}