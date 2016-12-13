package org.kp.cpc.pojos;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

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
	public String transportUri;

	public AgentMetadata(AgentConfig config, boolean paused) {
		this.title = config.getName();
		this.id = config.getId();
		this.agentId = config.getAgentId();
		this.paused = paused;
		this.transportUri = config.getTransportURI();
	}
	
	/**
	 * Constructor that expects a JSONObject in a particular format in order to initialize an AgentMetadata instance
	 * 
	 * @param json
	 */
	public AgentMetadata(JSONObject json) {
		try {
			this.title = json.getString("title");
			this.id = json.getString("id");
			this.agentId = json.getString("agentId");
			this.paused = json.getBoolean("paused");
			this.transportUri = json.getString("transportUri");
		} catch (JSONException e) {

		}
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