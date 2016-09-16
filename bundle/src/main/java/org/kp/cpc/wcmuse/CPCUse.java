package org.kp.cpc.wcmuse;

import java.util.ArrayList;
import java.util.List;

import org.kp.cpc.pojos.AgentGroup;
import org.kp.cpc.services.AgentGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.replication.AgentConfig;

public class CPCUse extends WCMUsePojo {
	private List<AgentGroup> agentGroups;
	private List<AgentConfig> allAgentConfigs;
	private List<String> groups = new ArrayList<String>();
	private static final Logger log = LoggerFactory.getLogger(CPCUse.class);
	
    @Override
    public void activate() throws Exception {
    	AgentGroupService ags = getSlingScriptHelper().getService(AgentGroupService.class);
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
}