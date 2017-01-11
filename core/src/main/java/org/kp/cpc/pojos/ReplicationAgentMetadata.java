package org.kp.cpc.pojos;

import java.util.List;

import com.day.cq.replication.AgentConfig;

public class ReplicationAgentMetadata extends AgentMetadata {
	public List<FlushAgentMetadata> flushAgents;
	
	public ReplicationAgentMetadata(AgentConfig config, boolean paused, List<FlushAgentMetadata> flushAgents) {
		this.title = config.getName();
		this.id = config.getId();
		this.agentId = config.getAgentId();
		this.paused = paused;
		this.transportUri = config.getTransportURI();
		this.flushAgents = flushAgents;
	}
	
	public List<FlushAgentMetadata> getFlushAgents() {
		return flushAgents;
	}
}
