package org.kp.cpc.services.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.kp.cpc.helpers.SharedConstants;
import org.kp.cpc.pojos.AgentGroup;
import org.kp.cpc.pojos.FlushAgentMetadata;
import org.kp.cpc.pojos.ReplicationAgentMetadata;
import org.kp.cpc.services.AgentGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.AgentManager;
import com.day.cq.replication.ReplicationQueue;

/**
 * OSGi service that manages custom groupings of Agents for use by the Content Publication Console
 * 
 * @author joshua.boyle
 */
@Component(label = "KP Content Publication Console - Agent Groupings", metatype = true, immediate = true)
@Service(AgentGroupServiceImpl.class)
public class AgentGroupServiceImpl implements AgentGroupService {
	/**
	 * String array of all agent groups as configured in Felix
	 */
	@Property(unbounded=PropertyUnbounded.ARRAY, label="Agent Groups", cardinality=10, description="Enter one group name per field entry.")
	private static final String AGENT_GROUPS = "agent.groups";
	private String[] agentGroupTitles;
	
	/**
	 * String array of comma-separated agents where each entry corresponds to the same index from agent.groups above
	 */
	@Property(unbounded=PropertyUnbounded.ARRAY, label="Agents per Group", cardinality=10, description="Each entry here corresponds to the same entry (by index) of the above Agent Groups.  Enter all agents you want in each group separated by commas.")
	private static final String AGENT_LISTS = "agent.lists";
	private String[] agentLists;

	/**
	 * After activation, will hold all AgentGroup instances per the Felix configurations
	 */
	private List<AgentGroup> agentGroups;
	
	@Reference
	private AgentManager agentMgr;
	
	@Reference
	private ResourceResolverFactory factory;

	private Map<String, Agent> agents;
	private List<AgentConfig> allAgentConfigs;
	
	private static final Logger log = LoggerFactory.getLogger(AgentGroupServiceImpl.class);
	
	/**
	 * Activation method for this OSGi service
	 * 
	 * @param properties	Properties as configured in the Felix console	
	 */
	@Activate
	protected void activate(Map<String, Object> properties) {
		agents = agentMgr.getAgents();
		agentGroupTitles = PropertiesUtil.toStringArray(properties.get("agent.groups"));
		agentLists = PropertiesUtil.toStringArray(properties.get("agent.lists"));
		
		if(null == agentGroupTitles || agentGroupTitles.length == 0) {
			agentGroupTitles = new String[1];
			agentGroupTitles[0] = "All";
		}
	}

	/**
	 * Helper method to return a Java List of all AgentGroups where an AgentGroup
	 * is a custom type consisting of a title and a Java List of AgentMetadata.
	 * 
	 * @return      		a List<AgentGroup> that contains all configured AgentGroups
	 * @see					AgentGroup
	 */
	public List<AgentGroup> getAgentGroups() {
		agentGroups = new ArrayList<AgentGroup>();

		if(null != agentLists) {
			// For each agent group that's specified in Felix
			for(int i = 0; i < agentGroupTitles.length; i++) {
				String[] agentIdsPerGroup = agentLists[i].split(",");
				
				// Will hold a List of ReplicationAgentMetadata where each instance represents a single replication agent configured on this author
				// instance.  Each replication agent on this author instance is then associated to n number of flush agents on its corresponding
				// publish instance.
				List<ReplicationAgentMetadata> replicationAgentMetasPerGroup = new ArrayList<ReplicationAgentMetadata>();
				
				// Each agent group title can have n number of actual replication agents configured to be part of that group so let's loop through the
				// the ids specified in Felix for this particular group
				for(int j = 0; j < agentIdsPerGroup.length; j++) {
					// If the Map of all agents we got from the AgentManager contains this id as it was specified via Felix
					// then let's build out an AgentMetadata object for this agent
					if(agents.containsKey(agentIdsPerGroup[j])) {
						Agent agent = agents.get(agentIdsPerGroup[j]);
						ReplicationQueue queue = agent.getQueue();
						
						// Each replication agent can have n number of associated flush agents on the publish instance it points to so we'll need to
						// get a List of FlushAgentMetadata first so we can build a ReplicationAgentMetadata object for this configured agent
						List<FlushAgentMetadata> flushAgentsPerReplicationAgent = new ArrayList<FlushAgentMetadata>();
						String publishUrl = agent.getConfiguration().getTransportURI();
						
						// Transport URIs will be in the form:  [protocol]://[server]:[port]/bin/receive?sling:authRequestLogin=1 so let's
						// parse out the publish instance's URL from it
						publishUrl = publishUrl.substring(0, publishUrl.indexOf("/bin/receive"));

						// For this replication agent's corresponding publish instance, let's call our publish-side servlet and get a JSONObject that holds
						// all the data we need about flush agents configured on that instance
						JSONObject response = getFlushJSON(publishUrl);
						
						try {
							JSONArray flushAgentsArr = response.getJSONArray("agents");
							int flushAgentCount = flushAgentsArr.length();
							
							// Let's build our List of FlushAgentMetadata that we can put inside our ReplicationAgentMetadata object that represents this
							// particular configured replication agent
							for(int k = 0; k < flushAgentCount; k++) {
								flushAgentsPerReplicationAgent.add(new FlushAgentMetadata(flushAgentsArr.getJSONObject(k)));
							}
						} catch (JSONException e) {
							log.error("JSONException caught in AgentGroupServiceImpl.getAgentGroups while attempting to read from flush agents");
						}
						
						// This completes our build of a single ReplicationAgentMetadata that holds n number of FlushAgentMetadata in a List
						// Let's add our new ReplicationAgentMetadata to our ongoing list of ReplicationAgentMetadata object for this one AgentGroup
						replicationAgentMetasPerGroup.add(new ReplicationAgentMetadata(agent.getConfiguration(), queue.isPaused(), flushAgentsPerReplicationAgent));
					}
				}
	
				// We've finished with one Felix-configured group of replication agents so let's add it to the ongoing List of AgentGroup and
				// move on to the next configured group!
				agentGroups.add(new AgentGroup(replicationAgentMetasPerGroup, agentGroupTitles[i]));
			}
		} else {
			log.error("No agent groups have been configured in the Felix console so we were unable to display any in the Content Publication Console.");
		}

		return agentGroups;
	}

	/**
	 * Makes a GET request to a single publish instance and returns the response as a JSON object
	 * 
	 * @param url			The url of the publish instance to which this GET request should be made
	 * 
	 * @return				a JSONObject containing the relevant information for any dispatcher flush agents configured on the publish instance
	 * @see					JSONObject
	 */
	private JSONObject getFlushJSON(String publishUrl) {
		JSONObject jsonResponse;

		try {			
			URL url = new URL(publishUrl + SharedConstants.FLUSH_SERVICE_ENDPOINT);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            InputStream content = (InputStream)connection.getInputStream();
            BufferedReader in = new BufferedReader (new InputStreamReader(content));
            String line;
            String total = "";
            while ((line = in.readLine()) != null) {
                total += line;
            }

            jsonResponse = new JSONObject(total);
		} catch(MalformedURLException e) {
			jsonResponse = new JSONObject();
			log.error("MalformedURLException caught in AgentGroupService.getFlushJSON");
		} catch(JSONException e) {
			jsonResponse = new JSONObject();
			log.error("JSONExceptoin caught in AgentGroupService.getFlushJSON");
		} catch(IOException e) {
			jsonResponse = new JSONObject();
			log.error("IOException caught in AgentGroupService.getFlushJSON");
		}
		
		return jsonResponse;
	}
	
	/**
	 * Helper method which returns a Java List of the AgentConfigs for 
	 * each agent on author (agents.author)
	 * 
	 * @return      		a List<AgentConfig> for every agent on author
	 * @see					AgentConfig
	 */
	public List<AgentConfig> getAllAgentConfigs() {
		allAgentConfigs = new ArrayList<AgentConfig>();
		
		Set<String> keys = agents.keySet();
		for(String key : keys) {
			allAgentConfigs.add(agents.get(key).getConfiguration());
		}

		return allAgentConfigs;
	}
}