package org.kp.cpc.pojos;

import com.day.cq.replication.AgentConfig;

/**
 * Helper POJO that encapsulates some basic information about a single Agent.
 * Used by AgentGroup.
 * 
 * @author joshua.boyle
 */
public class AgentMetadata {
	public String title;
	public String id;
	public String agentId;
	public String agentUserId;
	public boolean enabled;

	public AgentMetadata(AgentConfig config, boolean enabled) {
		this.title = config.getName();
		this.id = config.getId();
		this.agentId = config.getAgentId();
		this.agentUserId = config.getAgentUserId();
		this.enabled = enabled;
	}
	
	public String getTitle() {
		return title;
	}

	public String getId() {
		return id;
	}

	public String getAgentId() {
		return agentId;
	}

	public String getAgentUserId() {
		return agentUserId;
	}

	public boolean getEnabled() {
		return enabled;
	}
}