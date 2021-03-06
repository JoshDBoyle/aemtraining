package org.kp.cpc.servlets;

import java.io.IOException;

import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.kp.cpc.helpers.LastActivatedReport;
import org.kp.cpc.helpers.LastModifiedReport;
import org.kp.cpc.helpers.LockedReport;
import org.kp.cpc.helpers.SharedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.QueryBuilder;

/**
 * Path-based Sling Servlet that provides an entry point for building various
 * report.  Leverages helper classes for eachc report type to generate the
 * appropriate json or CSV content.
 * 
 * If additional report types are requested in the future, this class can be
 * expanded as the funnel through which all reporting requests initially flow
 * 
 * @author joshua.boyle
 */
@SlingServlet(
	    methods = { "GET" }, 
	    paths = {"/bin/cpc/buildreport" }, 
	    name = "org.kp.cpc.servlets.BuildReportServlet")
public class BuildReportServlet extends SlingAllMethodsServlet {
    static final long serialVersionUID = 1L;
    
    @Reference
    QueryBuilder qb;
    
    Logger log = LoggerFactory.getLogger(BuildReportServlet.class);
    
    @Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
    	ResourceResolver resolver = request.getResourceResolver();
    	Session session = resolver.adaptTo(Session.class);
    	String type = request.getParameter("type").toLowerCase();
    	try {	    	
	    	if(type.toLowerCase().equals(SharedConstants.ACTIVATION_REPORT)) {
	    		LastActivatedReport.buildReport(
	    				response, 
	    				session,
	    				qb, 
	    				request.getParameter("start"), 
	    				request.getParameter("end"),
	    				request.getParameter("csv"));
	    	} else if(type.toLowerCase().equals(SharedConstants.MODIFICATION_REPORT)) {
	    		LastModifiedReport.buildReport(
	    				response, 
	    				session, 
	    				qb, 
	    				request.getParameter("start"), 
	    				request.getParameter("end"),
	    				request.getParameter("csv"));
	    	} else if(type.equals(SharedConstants.LOCKED_REPORT)) {
	    		LockedReport.buildReport(
	    				response,
	    				session,
	    				qb,
	    				request.getParameter("csv"));
	    	}
    	} catch(Exception e) {
    		log.error("Generic exception caught in BuildReportServlet");
    		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("An error occured in BuildReportServlet.doGet.");
    	}	
    }
}