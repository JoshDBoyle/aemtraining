package org.kp.cpc.use;

import java.util.List;

import org.kp.cpc.services.impl.AgentGroupServiceImpl;

import com.day.cq.replication.AgentConfig;

/**
 * WCMUse used by all modals for the Content Publication Console
 * 
 * @author joshua.boyle
 */
public class ModalsUse extends CPCBaseUse {
	private List<AgentConfig> allAgentConfigs;

    @Override
    public void activate() throws Exception {
    	super.activate();

    	AgentGroupServiceImpl ags = getSlingScriptHelper().getService(AgentGroupServiceImpl.class);
    	if(null != ags) {
    		allAgentConfigs = ags.getAllAgentConfigs();
    	}
    }
    
    public List<AgentConfig> getAllAgentConfigs() {
    	return allAgentConfigs;
    }
}