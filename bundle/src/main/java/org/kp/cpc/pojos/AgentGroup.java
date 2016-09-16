package org.kp.cpc.pojos;

public class AgentGroup {
	String title;
	String[] agentIds;

	public AgentGroup(String[] agentIds, String title) {
		this.title = title;
		this.agentIds = agentIds;
	}
}