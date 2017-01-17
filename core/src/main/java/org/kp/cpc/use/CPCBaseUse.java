package org.kp.cpc.use;

import java.security.Principal;
import java.util.Iterator;

import javax.jcr.Session;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.jcr.base.util.AccessControlUtil;

import com.adobe.cq.sightly.WCMUsePojo;

/**
 * Base WCMUse class for the Content Publication Console Sightly component.
 * 
 * @author joshua.boyle
 */
public class CPCBaseUse extends WCMUsePojo {
	protected boolean canModify = false;
	private static final String canModifyGroup = "pubadmin";
	
    @Override
    public void activate() throws Exception {
    	Principal principal = getRequest().getUserPrincipal();
    	UserManager userMgr = AccessControlUtil.getUserManager(getResourceResolver().adaptTo(Session.class));
    	Authorizable authorizable = userMgr.getAuthorizable(principal);
    	Iterator<Group> it = authorizable.memberOf();
    	
    	while(it.hasNext()) {
    		Group group = it.next();
    		if(group.getID().equals(canModifyGroup)) {
    			canModify = true;
    		}
    	}
    }
    
    public boolean getCanModify() {
    	return canModify;
    }
}