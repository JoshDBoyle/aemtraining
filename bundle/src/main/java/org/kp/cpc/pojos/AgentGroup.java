package org.kp.cpc.pojos;

import java.util.List;

public class AgentGroup {
	public String title;
	public List<AgentMetadata> agentMetas;

	public AgentGroup(List<AgentMetadata> agentMetas, String title) {
		this.title = title;
		this.agentMetas = agentMetas;
	}
}