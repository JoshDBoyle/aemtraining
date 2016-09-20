package org.kp.cpc.wcmuse;

import java.util.ArrayList;
import java.util.List;

import org.kp.cpc.services.AgentGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.replication.AgentConfig;

public class ModalsUse extends WCMUsePojo {
	private List<AgentConfig> allAgentConfigs;
	private static final Logger log = LoggerFactory.getLogger(ModalsUse.class);

    @Override
    public void activate() throws Exception {
    	AgentGroupService ags = getSlingScriptHelper().getService(AgentGroupService.class);
    	if(null != ags) {
    		allAgentConfigs = ags.getAllAgentConfigs();
    	}
    }
    
    public List<AgentConfig> getAllAgentConfigs() {
    	return allAgentConfigs;
    }
}