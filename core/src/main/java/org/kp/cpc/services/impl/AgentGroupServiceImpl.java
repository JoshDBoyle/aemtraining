package org.kp.cpc.services.impl;

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
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.kp.cpc.pojos.AgentGroup;
import org.kp.cpc.pojos.AgentMetadata;
import org.kp.cpc.services.AgentGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.AgentManager;

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
				List<AgentMetadata> agentMetasPerGroup = new ArrayList<AgentMetadata>();
				
				// For each agentId per group, let's add them to a List<AgentMetadata> so we can build an official AgentGroup
				// Basically we're just converting from String[] to List<AgentGroup> here
				for(int j = 0; j < agentIdsPerGroup.length; j++) {
					// If the Map of all agents we got from the AgentManager contains this id as it was specified via Felix
					// Then let's get that Agent's AgentMetadata and add it to the List<AgentMetadata> for our AgentGroup
					if(agents.containsKey(agentIdsPerGroup[j])) {
						Agent agent = agents.get(agentIdsPerGroup[j]);
						ValueMap vm = resolver.resolve(agent.getConfiguration().getId() + "/" + JcrConstants.JCR_CONTENT).adaptTo(ValueMap.class);
						
						if(null != vm && vm.containsKey("enabled") && (vm.get("enabled", String.class).equals("true")))
							agentMetasPerGroup.add(new AgentMetadata(agent.getConfiguration(), true));
						else
							agentMetasPerGroup.add(new AgentMetadata(agent.getConfiguration(), false));
					}
				}
	
				agentGroups.add(new AgentGroup(agentMetasPerGroup, agentGroupTitles[i]));
			}
		} else {
			log.error("No agent groups have been configured in the Felix console so we were unable to display any in the Content Publication Console.");
		}

		return agentGroups;
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