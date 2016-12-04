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
	public boolean paused;

	public AgentMetadata(AgentConfig config, boolean paused) {
		this.title = config.getName();
		this.id = config.getId();
		this.agentId = config.getAgentId();
		this.paused = paused;
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

	public boolean getPaused() {
		return paused;
	}
}