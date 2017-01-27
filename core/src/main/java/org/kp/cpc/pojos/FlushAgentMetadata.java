package org.kp.cpc.pojos;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlushAgentMetadata extends AgentMetadata {

	Logger log = LoggerFactory.getLogger(FlushAgentMetadata.class);
	
	public FlushAgentMetadata(JSONObject json) {
		try {
			this.title = json.getString("title");
			this.id = json.getString("id");
			this.agentId = json.getString("agentId");
			this.transportUri = json.getString("transportUri");
		} catch (JSONException e) {
			log.error("JSONException caught in FlushAgentMetadata constructor");
		}
	}
}
