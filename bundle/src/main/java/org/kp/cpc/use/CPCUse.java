package org.kp.cpc.use;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kp.cpc.models.AgentGroup;
//import org.kp.cpc.models.AgentGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentManager;

public class CPCUse extends WCMUsePojo {
	private List<AgentGroup> agentGroups = new ArrayList<AgentGroup>();
	private Map<String, Agent> agentsMap;
	private List<String> groups = new ArrayList<String>();
	private static final Logger log = LoggerFactory.getLogger(CPCUse.class);
	
    @Override
    public void activate() throws Exception {
    	log.error("I AM IN ACTIVATE OF CPCUSE");
    }
}