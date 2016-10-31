package org.kp.cpc.pojos;

import java.util.List;

/**
 * Helper POJO that encapsulates a group of Agents for consumption by the client.
 * 
 * @author joshua.boyle
 */
public class AgentGroup {
	public String title;
	public List<AgentMetadata> agentMetas;

	public AgentGroup(List<AgentMetadata> agentMetas, String title) {
		this.title = title;
		this.agentMetas = agentMetas;
	}
}