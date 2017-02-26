/**
 * Clears the cache for a single dispatcher
 */
function clearCache(deleteCacheBtn) {
	var $agent = $(deleteCacheBtn).closest('.agent');

	$.post('/bin/cpc/flushcache', { 'replicationAgentId': $agent.attr('data-agent'), 'flushAgentId': $(deleteCacheBtn).attr('data-id') }, function(data) {
		if(null != data) {
			var $container = $(deleteCacheBtn).closest('coral-actionbar-container');
			var $success = $container.find('.cache-deletion-success');
			var $failure = $container.find('.cache-deletion-failure');
			
			// Bumping the margin-top 8px is a hack to account for the way CoralUI handles placement of coral-actionbar-items on display
			$(deleteCacheBtn).css('margin-top', '8px');
			
			if(data.status == '502') {
				$success.hide();
				$failure.show();
			} else if(data.status == '200') {
				$failure.hide();
				$success.show();
			}
		}
	});
}

/**
 * Sets the status light for an individual agent
 */
function setAgentStatus($agent, queueData, agentData) {
	var blocked = queueData ? queueData.metaData.queueStatus.isBlocked : false;
	var standby = agentData.standby ? true : false;
	var enabled = agentData.enabled ? agentData.enabled : false;
	var $status = $agent.find('.led-box div');

	$status.removeClass();
	if(blocked && !standby && enabled) {
		$status.addClass('led-red');
	} else if(!blocked && !standby && enabled && queueData.queue.length > 0) {
		$status.addClass('led-blue');
	} else if(!enabled) {
		$status.addClass('led-grey');
	} else if(standby) {
		$status.addClass('led-yellow');
	} else if(!blocked && !standby && enabled) {
		$status.addClass('led-green');
	}
}

/**
 * Refreshes the queue for an individual agent
 */
function refreshQueue($agent) {
	$.getJSON('/etc/replication/agents.author/' + $agent.attr('data-agent') + '/jcr:content.queue.json').success(function(queueSuccess) {
		var queueData = queueSuccess;
		$.getJSON('/etc/replication/agents.author/' + $agent.attr('data-agent') + '/jcr:content.json').success(function(agentSuccess) {
			var agentData = agentSuccess;
			var $agentTemp = $("[data-agent='" + queueData.metaData.queueStatus.agentId + "']");
			var $queue = $agentTemp.find('.agent-queue').eq(0);

			setAgentStatus($agentTemp, queueData, agentData);
			$queue.empty();

			if(queueData.queue.length > 0) {
				for(var j = 0; j < queueData.queue.length; j++) {
					$queue.append("<a target='_blank' href='" + queueData.queue[j].path + ".html'>" + queueData.queue[j].path + "</a>" + "<span>" + queueData.queue[j].type + "</span>");
				}
			}
		});
	});
}

/**
 * Wrapper that refreshes each queue for all agents
 */
function refreshQueues() {
	var $agents = $('.agent');
	for(var i = 0; i < $agents.length; i++) {
		refreshQueue($agents.eq(i));
	}
}

/**
 * Entry listener for document.ready where we establish our CoralUI objects and add all listeners
 */
$(document).ready(function() {
  var checkedCount = 0;

  var legendModal = new CUI.Modal({
	 element : '#legend-modal',
	 visible : false
  });

  var agentsInfoModal = new CUI.Modal({
    element : '#agents-info-modal',
    visible : false
  });

  var queryByDateModal = new CUI.Modal({
	 element : '#query-by-date-modal',
	 visible : false
  });
  
  /**
   * COMMAND BAR MODAL LISTENERS
   */
  $('#legend-button').on('click', function(event) {
	  legendModal.show();
  });
  
  $('#all-agents-list-button').on('click', function(event) {
	  agentsInfoModal.show();
  });

  $('#query-by-date-button').on('click', function(event) {
	  queryByDateModal.show();
  });

  /**
   * INDIVIDUAL AGENT STANDBY AND ENABLED TOGGLING
   */
  $('.agent-toggle-standby, .agent-toggle-enabled').on('click', function(event) {
	  var toggle = event.currentTarget;
	  var id = toggle.getAttribute('data-id');
	  var type = toggle.classList.contains('agent-toggle-standby') ? 'standby' : 'enabled';
	  var checked = type == 'standby' ? toggle.checked : !toggle.checked;
	  var $agent = $(toggle).closest('.agent');
	  
	  $.post("/bin/cpc/updateagent", { 'id': id, 'type': type, 'value': checked }, function(data) {
		  refreshQueue($agent);
	  });
  });

  /**
   * STANDBY AND ENABLED TOGGLING BY AGENT GROUP
   */
  $('#pause-group-btn, #unpause-group-btn, #enable-group-btn, #disable-group-btn').on('click', function(event) {
	  var groupToggle = event.currentTarget;
	  var type = groupToggle.id == 'pause-group-btn' || groupToggle.id == 'unpause-group-btn' ? 'standby' : 'enabled';
	  var individualToggles = $(groupToggle).closest('coral-panel-content')[0].querySelectorAll('.agent-toggle-' + type);
	  var value = (groupToggle.id == 'pause-group-btn' || groupToggle.id == 'enable-group-btn') ? true : false;

	  for(var i = 0; i < individualToggles.length; i++) {
		  var toggle = individualToggles[i];
		  var $agent = $(individualToggles[i]).closest('.agent');

		  toggle.checked = type == 'standby' ? !value : value;
		  
		  $.post("/bin/cpc/updateagent", { 'id': $agent.attr('data-agent'), 'type': type, 'value': value }, function(data) {
			  if(data.agentId && data.agentId !== '') {
				  refreshQueue($("coral-masonry-item[data-agent='" + data.agentId +"']").eq(0));
			  }
		  });
	  }
  });
  
  /**
   * INDIVIDUAL CACHE DELETION
   */
  $('.delete-cache-btn').on('click', function(event) {
	  clearCache(event.currentTarget);
  });

  /**
   * CACHE DELETION BY GROUP
   */
  $('#delete-group-cache-btn').on('click', function(event) {
	  var groupToggle = event.currentTarget;
	  var individualToggles = $(groupToggle).closest('coral-panel-content')[0].querySelectorAll('.delete-cache-btn');

	  for(var i = 0; i < individualToggles.length; i++) {
		  clearCache(individualToggles[i]);  
	  }
  });
  
  /**
   * VIEW AGENT LOG BUTTON
   */
  $('.view-log-btn').on('click', function(event) {
	  var $this = $(this);
	  
	  $.get('/bin/cpc/viewagentlog', { id: $(this).closest('.agent').attr('data-agent') }, function(data) {
		  var $well = $this.closest('.agent').find('.agent-queue').first();
		  
		  $well.empty();
		  $well.append(data);
	  });
  });
  
  $('.view-queue-btn').on('click', function(event) {
	  refreshQueue($(this).closest('.agent'));
  });
  
  /**
   * REPORTING OPTIONS WITHIN REPORT MODAL
   */
  $('.query-by-date-button, .query-by-date-button-csv').on('click', function(event) {
	  var start = $('#startdate').val();
	  var end = $('#enddate').val();
	  var csv = event.currentTarget.classList.contains('query-by-date-button-csv');
	  var type = $('#report-type').val();
	  var $activateSelectedBtn = $('#activate-selected-btn');

	  checkedCount = 0;
	  $activateSelectedBtn.attr('disabled', true);
	  
	  $.get("/bin/cpc/querybydate", { 'start': start, 'end': end, 'csv': csv, 'type': type }, function(data) {
		  var results = $('#query-by-date-results');
		  
		  $('#select-all').off();
		  
		  results.empty();
		  if(null == data.results) {
			  var a         = document.createElement('a');
			  a.href        = 'data:attachment/csv,' +  encodeURIComponent(data);
			  a.target      = '_blank';
			  a.download    = 'report.csv';

			  document.body.appendChild(a);
			  a.click();
			  $(a).remove();
		  } else {
			  var table = 	"<table class='coral-Table'>" +
			  				"	<thead>" +
			  				"		<tr class='coral-Table-row'>" +
						  	"			<th class='coral-Table-headerCell'>" +
						  	"				<label class='coral-Checkbox'>" + 
				  			"					<input id='select-all' class='coral-Checkbox-input' type='checkbox' name='c2' value='2'>" +
				  			"					<span class='coral-Checkbox-checkmark'></span>" +
				  			"					<span class='coral-Checkbox-description'></span>" +
				  			"				</label>" +
				  			"			</th>";

			  for(var i = 0; i < data.headers.length; i++) {
				  table += "<th class='coral-Table-headerCell'>" + data.headers[i] + "</th>";
			  }

			  table += "<th class='coral-Table-headerCell'>Debug</th>"
			  table += "</tr></thead><tbody>";

			  for(var i = 0; i < data.results.length; i++) {
				  table +=	"<tr class='coral-Table-row'>" +
				  			"	<td class='coral-Table-cell'>" +
				  			"		<label class='coral-Checkbox'>" + 
				  			"			<input class='select-one coral-Checkbox-input' type='checkbox' name='c2' value='2'>" +
				  			"			<span class='coral-Checkbox-checkmark'></span>" +
				  			"			<span class='coral-Checkbox-description'></span>" +
				  			"		</label>" +
				  			"   </td>" +
		  					"	<td class='content-path coral-Table-cell'><a href='" + data.results[i].path + ".html'>" + data.results[i].path + "</a></td>" +
		  					"	<td class='coral-Table-cell'>" + data.results[i].columnb + "</td>" +
		  					"	<td class='coral-Table-cell'>" + data.results[i].columnc + "</td>" +
		  					"	<td class='coral-Table-cell'>" +
		  					" 		<button class='debug-content coral-Button coral-Button--square coral-Button--primary'>" +
		  					"			<i class='coral-Icon coral-Icon--globe'></i>" +
		  					"		</button>" + 
		  					"	</td>" +
		  					"</tr>";
			  }

			  table += '</tbody></table>';

			  results.append(table);
			  queryByDateModal.center();
			  
		  }
		  
		  /**
		   * SELECT ALL LISTENER FOR REPORT MODAL
		   * 
		   * This listener is added here as opposed to document.ready because it won't bind until there's results
		   */
		  $('#select-all').on('click', function(event) {
			  var checked = $(this).is(':checked');
			  $('.select-one').each(function() {
		      	$(this).prop('checked', checked);
		      	if(checked)
		      		checkedCount += 1;
		      	else
		      		checkedCount -= 1;
		      	
		      	if(checkedCount >= 1)
		      		$activateSelectedBtn.attr('disabled', false);
		      	else
		      		$activateSelectedBtn.attr('disabled', true);
		      });
		  });
		  
		  /**
		   * SELECT ONE ROW LISTENER FOR REPORT MODAL
		   * 
		   * This listener is added here as opposed to document.ready because it won't bind until there's results
		   */
		  $('.select-one').on('click', function(event) {
			  if($(this).is(':checked'))
				  checkedCount += 1;
			  else
				  checkedCount -= 1;
			  
			  if(checkedCount >= 1)
				  $activateSelectedBtn.attr('disabled', false);
			  else
				  $activateSelectedBtn.attr('disabled', true);
		  });
		  
		  /**
		   * DEBUG ONE ROW LISTENER FOR REPORT MODAL
		   * 
		   * When clicking the debug button for a row within the results table of this modal, this listener will open up that row's
		   * content path in new tabs on author, on the chosen publish instance, and on any dispatchers associated with that publish
		   * instance simultaneously.  This feature is used to compare content across an entire "stack" or "path" to see if there are
		   * any content discrepancies.
		   */
		  $('.debug-content').on('click', function(event) {
			  var publishUrl = $('#chosen-publish').val();
			  var text = $("#chosen-publish option:selected").text();
			  var path = $(this).parent().siblings('.content-path').eq(0).children('a').eq(0).attr('href');

			  var $flushAgents = $(".agent[data-agent='" + text +"'] .flush-agent");
			  
			  window.open(path + '?wcmmode=preview', '_blank'); 					// Open on author (only need path because if we're staying on same instance, AEM will handle the rest
			  window.open(publishUrl + path, '_blank'); 	// Open on chosen publish instance (need full URL since we're leaving the instance)
			  
			  // For this publish instance, we may have multiple dispatchers so let's loop through them
			  $.each($flushAgents, function(idx, val) {
				  var transportUri = $(val).attr('data-transporturi');
				  window.open(transportUri.substring(0, transportUri.indexOf('/dispatcher')) + path, '_blank');
			  });
		  });
	  });
  });

  /**
   * ACTIVATE SELECTED ROWS LISTENER FOR REPORT MODAL
   * 
   * For each selected row's content path, performs replication
   */
  $('#activate-selected-btn').on('click', function(event) {
	  var $selectedInputs = $('#query-by-date-results .select-one:checked');
	  var pathsToReplicate = '';
	  
	  $selectedInputs.each(function() {
		  pathsToReplicate += $(this).parent().parent().next().text() + ',';
	  });
	  	
	  $.post('/bin/cpc/activateselected', { 'paths': pathsToReplicate.slice(0, -1) }, function(data) {
		  alert(data);
	  });
  });

  refreshQueues();
})