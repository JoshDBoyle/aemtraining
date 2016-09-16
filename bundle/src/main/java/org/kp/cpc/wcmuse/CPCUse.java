package org.kp.cpc.wcmuse;

import java.util.ArrayList;
import java.util.List;

import org.kp.cpc.pojos.AgentGroup;
import org.kp.cpc.services.AgentGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.replication.Agent;

public class CPCUse extends WCMUsePojo {
	private List<AgentGroup> agentGroups;
	private List<String> groups = new ArrayList<String>();
	private static final Logger log = LoggerFactory.getLogger(CPCUse.class);
	
    @Override
    public void activate() throws Exception {
    	AgentGroupService ags = getSlingScriptHelper().getService(AgentGroupService.class);
    	if(null != ags) {
    		agentGroups = ags.getAgentGroups();
    	}
    	log.info("Received AgentGroups from AgentGroupService");
    }
    
    public List<AgentGroup> getAgentGroups() {
    	return agentGroups;
    }
}