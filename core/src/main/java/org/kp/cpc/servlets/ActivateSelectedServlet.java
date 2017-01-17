package org.kp.cpc.servlets;

import java.io.IOException;

import javax.jcr.Session;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	    paths = {"/bin/cpc/activateselected" }, 
	    name = "org.kp.cpc.services.ActivateSelectedServlet")
public class ActivateSelectedServlet extends SlingAllMethodsServlet {
    static final long serialVersionUID = 1L;

    @Reference
    Replicator replicator;
    
    Logger log = LoggerFactory.getLogger(ActivateSelectedServlet.class);
    
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	String[] paths = request.getParameter("paths").split(",");

    	if(null != paths && paths.length > 0) {
    		Session session = request.getResourceResolver().adaptTo(Session.class);
    		if(null != session) {
	    		for(String path : paths) {
	    			try {
	    				replicator.replicate(session, ReplicationActionType.ACTIVATE, path);
	    			} catch(ReplicationException e) {
	    	    		log.error("ReplicationException caught in ActivateSelectedServlet while trying to activate the following path from the CPC Report Modal: " + path);
	    	    		response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    	    		response.getWriter().write("A ReplicationException was thrown and caught during activation.  The selected paths were not activated.");
	    	    		return;
	    	    	}
	    		}
    		} else {
    			log.error("Unable to acquire a session off request.getResourceResolver() via adaptation in ActivateSelectedServlet.");
    			response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    		response.getWriter().write("Unable to acquire a session.  The selected paths were not activated.");
	    		return;
    		}
    	}

    	response.setStatus(SlingHttpServletResponse.SC_OK);
		response.getWriter().write("The selected paths were successfully queued for activation");
    }
}