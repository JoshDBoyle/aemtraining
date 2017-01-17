package org.kp.cpc.pojos;

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
	public String transportUri;
	
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
	
	public String getTransportUri() {
		return transportUri;
	}
}