package org.kp.cpc.use;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Session;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.kp.cpc.pojos.AgentGroup;
import org.kp.cpc.pojos.ReplicationAgentMetadata;
import org.kp.cpc.services.impl.AgentGroupServiceImpl;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.replication.AgentConfig;

/**
 * Base WCMUse class for the Content Publication Console Sightly component.
 * 
 * @author joshua.boyle
 */
public class CPCBaseUse extends WCMUsePojo {
	protected boolean canModify = false;
	protected boolean canClear = false;

	private static final String canModifyGroup = "pubadmin";
	private static final String canClearGroup = "cacheadmin";
	
	protected List<AgentGroup> agentGroups;
	protected List<AgentConfig> allAgentConfigs;

	public List<Publisher> publishers;
	
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
    		} else if(group.getID().equals(canClearGroup)) {
    			canClear = true;
    		}
    	}

    	AgentGroupServiceImpl ags = getSlingScriptHelper().getService(AgentGroupServiceImpl.class);
    	if(null != ags) {
    		agentGroups = ags.getAgentGroups(getResourceResolver());
    		allAgentConfigs = ags.getAllAgentConfigs();
    	}
    	
    	publishers = new ArrayList<CPCBaseUse.Publisher>();
    	for(AgentGroup group : agentGroups) {
    		for(ReplicationAgentMetadata ram : group.replicationAgentMetas) {
    			String transportUri = ram.getTransportUri();
 
    			publishers.add(new Publisher(transportUri.substring(0, transportUri.indexOf("/bin/receive")), ram.getAgentId()));
    		}
    	}
    }

    public boolean getCanModify() {
    	return canModify;
    }

    public boolean getCanClear() {
    	return canClear;
    }
    
    public List<AgentGroup> getAgentGroups() {
    	return agentGroups;
    }
    
    public List<AgentConfig> getAllAgentConfigs() {
    	return allAgentConfigs;
    }
    
    public List<Publisher> getPublishers() {
    	return publishers;
    }
    
    /**
     *	Inner helper/convenience pojo to encapsulate useful information about a publish instance
     */
    public class Publisher {
    	public String url;
    	public String id;
    	
    	public Publisher(String url, String id) {
    		this.url = url;
    		this.id = id;
    	}
    	
    	public String getUrl() {
    		return url;
    	}
    	
    	public String getId() {
    		return id;
    	}
    }
}