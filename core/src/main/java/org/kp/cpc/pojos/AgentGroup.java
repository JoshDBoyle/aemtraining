package org.kp.cpc.pojos;

import java.util.List;

/**
 * Helper POJO that encapsulates a group of Agents for consumption by the client.
 * 
 * @author joshua.boyle
 */
public class AgentGroup {
	public String title;
	public List<AgentMetadata> replicationAgentMetas;
	public List<AgentMetadata> flushAgentMetas;

	public AgentGroup(List<AgentMetadata> replicationAgentMetas, List<AgentMetadata> flushAgentMetas, String title) {
		this.title = title;
		this.replicationAgentMetas = replicationAgentMetas;
		this.flushAgentMetas = flushAgentMetas;
	}
}