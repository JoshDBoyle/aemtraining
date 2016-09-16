package org.kp.cpc.services;

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
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.kp.cpc.pojos.AgentGroup;
import org.kp.cpc.pojos.AgentMetadata;

import com.day.cq.replication.Agent;
import com.day.cq.replication.AgentConfig;
import com.day.cq.replication.AgentManager;

@Component(label = "A service for configuring Replication Agent groups", metatype = true, immediate = true)
@Service(AgentGroupService.class)
public class AgentGroupService {
	@Property(unbounded=PropertyUnbounded.ARRAY, label="Replication Agent Groups", cardinality=10, description="Enter one group name per field entry.")
	private static final String AGENT_GROUPS = "agent.groups";
	private String[] agentGroupTitles;
	
	@Property(unbounded=PropertyUnbounded.ARRAY, label="Replication Agents per Group", cardinality=10, description="Each entry here corresponds to the same entry (by index) of the above Replication Groups.  Enter all agents you want in each group separated by commas.")
	private static final String AGENT_LISTS = "agent.lists";
	private String[] agentLists;

	private List<AgentGroup> agentGroups = new ArrayList<AgentGroup>();
	
	@Reference
	AgentManager agentMgr;

	private Map<String, Agent> agents;
	private List<AgentConfig> allAgentConfigs = new ArrayList<AgentConfig>();
	
	@Activate
	protected void activate(Map<String, Object> properties) {
		this.agents = agentMgr.getAgents();
		this.agentGroupTitles = PropertiesUtil.toStringArray(properties.get("agent.groups"));
		this.agentLists = PropertiesUtil.toStringArray(properties.get("agent.lists"));
		
		if(null == this.agentGroupTitles || this.agentGroupTitles.length == 0) {
			this.agentGroupTitles = new String[1];
			this.agentGroupTitles[0] = "All";
		}

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
					agentMetasPerGroup.add(new AgentMetadata(agent.getConfiguration()));
				}
			}

			agentGroups.add(new AgentGroup(agentMetasPerGroup, agentGroupTitles[i]));
		}
		
		Set<String> keys = agents.keySet();
		for(String key : keys) {
			allAgentConfigs.add(agents.get(key).getConfiguration());
		}
	}

	public List<AgentGroup> getAgentGroups() {
		return agentGroups;
	}
	
	public List<AgentConfig> getAllAgentConfigs() {
		return allAgentConfigs;
	}
}