package org.kp.cpc.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyUnbounded;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.kp.cpc.pojos.AgentGroup;

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
	
	@Activate
	protected void activate(Map<String, Object> properties) {
		this.agentGroupTitles = PropertiesUtil.toStringArray(properties.get("agent.groups"));
		this.agentLists = PropertiesUtil.toStringArray(properties.get("agent.lists"));
		
		if(null == this.agentGroupTitles || this.agentGroupTitles.length == 0) {
			this.agentGroupTitles = new String[1];
			this.agentGroupTitles[0] = "All";
		}

		for(int i = 0; i < agentGroupTitles.length; i++) {
			agentGroups.add(new AgentGroup(agentLists[i].split(","), agentGroupTitles[i]));
		}
	}

	public List<AgentGroup> getAgentGroups() {
		return this.agentGroups;
	}
}