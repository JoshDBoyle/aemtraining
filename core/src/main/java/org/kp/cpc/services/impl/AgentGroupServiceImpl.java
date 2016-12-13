package org.kp.cpc.services.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.commons.json.JsonParser;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.JSONTokener;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.xss.JSONUtil;
import org.kp.cpc.helpers.SharedConstants;
import org.kp.cpc.pojos.AgentGroup;
import org.kp.cpc.pojos.AgentMetadata;
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
	AgentManager agentMgr;
	
	@Reference
	ResourceResolverFactory factory;

	private Map<String, Agent> agents;
	private List<AgentConfig> allAgentConfigs;
	
	private ResourceResolver resolver;
	
	private static final Logger log = LoggerFactory.getLogger(AgentGroupServiceImpl.class);
	
	/**
	 * Activation method for this OSGi service
	 * 
	 * @param properties	Properties as configured in the Felix console	
	 */
	@Activate
	protected void activate(Map<String, Object> properties) {
		try {
			resolver = factory.getAdministrativeResourceResolver(null);
		} catch (LoginException e) {
			e.printStackTrace();
		}

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
			// For each group title that was specified via Felix, build a List<AgentGroup> of all the specified (in Felix) agents per group
			for(int i = 0; i < agentGroupTitles.length; i++) {
				String[] agentIdsPerGroup = agentLists[i].split(",");
				List<AgentMetadata> replicationAgentMetasPerGroup = new ArrayList<AgentMetadata>();
				List<AgentMetadata> flushAgentMetasPerGroup = new ArrayList<AgentMetadata>();
				
				// For each agentId per group, let's add them to a List<AgentMetadata> so we can build an official AgentGroup
				// Basically we're just converting from String[] to List<AgentGroup> here
				for(int j = 0; j < agentIdsPerGroup.length; j++) {
					// If the Map of all agents we got from the AgentManager contains this id as it was specified via Felix
					// Then let's get that Agent's AgentMetadata and add it to the List<AgentMetadata> for our AgentGroup
					if(agents.containsKey(agentIdsPerGroup[j])) {
						Agent agent = agents.get(agentIdsPerGroup[j]);
						ReplicationQueue queue = agent.getQueue();
						replicationAgentMetasPerGroup.add(new AgentMetadata(agent.getConfiguration(), queue.isPaused()));
						//TODO: Call the new path-based servlet on the publish instance this replication agent points to.
						//		For each flush agent it returns, add a new AgentMetadata to flushAgentMetasPerGroup.  I
						//		won't be able to pass a Java object over http (so I can't pass back an AgentConfig and use
						//		that to and queue.isPaused() to instantiate a new AgentMetadata) so I'll add a new constructor
						//		to AgentMetadata that sets the member variables individually and I'll just retrieve a JSON
						//		object of the stuff I need from the flush agent.
						
						//The agent's transport URI will be in the form:  http://localhost:4503/bin/receive?sling:authRequestLogin=1
						String publishUrl = agent.getConfiguration().getTransportURI();
						publishUrl = publishUrl.substring(0, publishUrl.indexOf("/bin/receive"));

						JSONObject response = getFlushJSON(publishUrl);
						
						//A dispatcher flush agent transportURI will be in the form:  https://xlzxped0016x.lvdc.kp.org:44301/dispatcher/invalidate.cache
						try {
							JSONArray flushAgents = response.getJSONArray("agents");
							int flushAgentCount = flushAgents.length();
							for(int k = 0; k < flushAgentCount; k++) {
								flushAgentMetasPerGroup.add(new AgentMetadata(flushAgents.getJSONObject(k)));
							}
						} catch (JSONException e) {
							log.error("JSONException caught in AgentGroupServiceImpl.getAgentGroups while attempting to read from flush agents");
						}
					}
				}
	
				agentGroups.add(new AgentGroup(replicationAgentMetasPerGroup, flushAgentMetasPerGroup, agentGroupTitles[i]));
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
			Scanner scanner = new Scanner(url.openStream());
			String response = scanner.useDelimiter("\\Z").next();
			jsonResponse = new JSONObject(response);
			scanner.close();
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