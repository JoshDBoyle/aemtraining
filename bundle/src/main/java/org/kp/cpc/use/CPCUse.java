package org.kp.cpc.use;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Session;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.kp.cpc.pojos.AgentGroup;
import org.kp.cpc.services.impl.AgentGroupServiceImpl;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.replication.AgentConfig;

public class CPCUse extends WCMUsePojo {
	private List<AgentGroup> agentGroups;
	private List<AgentConfig> allAgentConfigs;
	private boolean canModify = false;
	private static final String canModifyGroup = "pubadmin";
	
    @Override
    public void activate() throws Exception {
    	AgentGroupServiceImpl ags = getSlingScriptHelper().getService(AgentGroupServiceImpl.class);
    	if(null != ags) {
    		agentGroups = ags.getAgentGroups();
    		allAgentConfigs = ags.getAllAgentConfigs();
    	}

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

    public List<AgentGroup> getAgentGroups() {
    	return agentGroups;
    }
    
    public List<AgentConfig> getAllAgentConfigs() {
    	return allAgentConfigs;
    }
    
    public boolean getCanModify() {
    	return canModify;
    }
}