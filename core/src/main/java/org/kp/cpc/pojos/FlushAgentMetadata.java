package org.kp.cpc.pojos;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

public class FlushAgentMetadata extends AgentMetadata {
	public FlushAgentMetadata(JSONObject json) {
		try {
			this.title = json.getString("title");
			this.id = json.getString("id");
			this.agentId = json.getString("agentId");
			this.paused = json.getBoolean("paused");
			this.transportUri = json.getString("transportUri");
		} catch (JSONException e) {
			
		}
	}
}
