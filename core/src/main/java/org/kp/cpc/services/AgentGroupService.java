package org.kp.cpc.services;

import java.util.List;

import org.apache.sling.api.resource.ResourceResolver;
import org.kp.cpc.pojos.AgentGroup;

import com.day.cq.replication.AgentConfig;

/**
 * Interface for AgentGroupServiceImpl
 * 
 * @author joshua.boyle
 */
public interface AgentGroupService {

	public List<AgentGroup> getAgentGroups(ResourceResolver resolver);
	public List<AgentConfig> getAllAgentConfigs();

}
