package org.kp.cpc.services;

import java.util.List;

import org.kp.cpc.pojos.AgentGroup;

import com.day.cq.replication.AgentConfig;

public interface AgentGroupService {

	public List<AgentGroup> getAgentGroups();
	public List<AgentConfig> getAllAgentConfigs();

}
