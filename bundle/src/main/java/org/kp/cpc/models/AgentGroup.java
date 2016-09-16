package org.kp.cpc.models;

public class AgentGroup {
	String title;
	String[] agentIds;

	public AgentGroup(String[] agentIds, String title) {
		this.title = title;
		this.agentIds = agentIds;
	}
}