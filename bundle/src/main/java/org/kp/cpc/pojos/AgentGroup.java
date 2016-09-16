package org.kp.cpc.pojos;

public class AgentGroup {
	public String title;
	public String[] agentIds;

	public AgentGroup(String[] agentIds, String title) {
		this.title = title;
		this.agentIds = agentIds;
	}
}