package org.kp.cpc.use;

import java.util.List;

import org.kp.cpc.pojos.AgentGroup;
import org.kp.cpc.services.impl.AgentGroupServiceImpl;

import com.day.cq.replication.AgentConfig;

/**
 * WCMUse class for the Content Publication Console Sightly component.
 * 
 * @author joshua.boyle
 */
public class CPCUse extends CPCBaseUse {
	private List<AgentGroup> agentGroups;
	private List<AgentConfig> allAgentConfigs;
	
    @Override
    public void activate() throws Exception {
    	super.activate();
    	AgentGroupServiceImpl ags = getSlingScriptHelper().getService(AgentGroupServiceImpl.class);
    	if(null != ags) {
    		agentGroups = ags.getAgentGroups();
    		allAgentConfigs = ags.getAllAgentConfigs();
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