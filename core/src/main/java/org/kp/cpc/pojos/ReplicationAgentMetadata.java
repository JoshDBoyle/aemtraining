package org.kp.cpc.pojos;

import java.util.List;

import com.day.cq.replication.AgentConfig;

public class ReplicationAgentMetadata extends AgentMetadata {
	public List<FlushAgentMetadata> flushAgents;
	
	public ReplicationAgentMetadata(AgentConfig config, boolean standby, List<FlushAgentMetadata> flushAgents) {
		this.title = config.getName();
		this.id = config.getId();
		this.agentId = config.getAgentId();
		this.standby = standby;
		
		if(standby)
			this.transportUri = config.getProperties().get("standby", String.class);
		else
			this.transportUri = config.getTransportURI();

		this.flushAgents = flushAgents;
	}
	
	public List<FlushAgentMetadata> getFlushAgents() {
		return flushAgents;
	}
}
