package org.kp.cpc.pojos;

import com.day.cq.replication.AgentConfig;

public class AgentMetadata {
	public String title;
	public String id;
	public String agentId;
	public String agentUserId;
	public boolean enabled;

	public AgentMetadata(AgentConfig config) {
		this.title = config.getName();
		this.id = config.getId();
		this.agentId = config.getAgentId();
		this.agentUserId = config.getAgentUserId();
	}
}