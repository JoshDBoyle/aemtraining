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
function setAgentStatus($agent, data) {
	var blocked = data.blocked;
	var standby = data.standby;
	var enabled = data.enabled;
	var $status = $agent.find('.led-box div');
	
	console.log("Agent:   " + data.agentId);
	console.log("Blocked: " + data.blocked);
	console.log("Standby: " + data.standby);
	console.log("Enabled: " + data.enabled);
	
	$status.removeClass();
	if(blocked && !standby && enabled) {
		$status.addClass('led-red');
	} else if(!blocked && !standby && enabled && null != data.queue && data.queue.length > 0) {
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
	$.getJSON('/bin/cpc/agentmetadata', {'agentId': $agent.attr('data-agent')}).success(function(data) {
		$queue = $agent.find('.agent-queue').eq(0);
		
		$queue.empty();
		if(null != data && null != data.queue && data.queue.length > 0) {
			for(var i = 0; i < data.queue.length; i++) {
				$queue.append("<a target='_blank' href='" + data.queue[i].path + ".html'>" + data.queue[i].path + "</a>" + "<span>" + data.queue[i].type + "</span>");
			}
		}

		setAgentStatus($agent, data);
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
  
  /**
   * COMMAND BAR MODAL LISTENERS
   */
  $('#legend-btn').on('click', function(event) {
	  $('#legend-modal')[0].show();
  });
  
  $('#agent-list-btn').on('click', function(event) {
	  $('#agent-list-modal')[0].show();
  });

  $('#reporting-btn').on('click', function(event) {
	  $('#reporting-modal')[0].show();
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
		  
		  $.post("/bin/cpc/updateagent", { 'id': $agent.attr('data-agent'), 'type': type, 'value': value }).success(function(data) {
			  if(data.agentId && data.agentId !== '') {
				  setTimeout(function() {
					  refreshQueue($("coral-masonry-item[data-agent='" + data.agentId +"']").eq(0));
				  }, 200);
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
  $('#query-btn, #query-to-csv-btn').on('click', function(event) {
	  var start = $('#startdate').val();
	  var end = $('#enddate').val();
	  var csv = event.currentTarget.id == 'query-to-csv-btn';
	  var type = $('#report-type').val();
	  var $activateSelectedBtn = $('#activate-selected-btn');
	  var $unlockSelectedBtn = $('#unlock-selected-btn');
	  var $results = $('#results');
	  var height = $results.parent().parent().outerHeight() - $results.prev().outerHeight() - $results.parent().prev().outerHeight();

	  checkedCount = 0;
	  $activateSelectedBtn.attr('disabled', true);
	  $('#results').css('height', height);
	  
	  $('#wait-overlay').css('display', 'block');
	  $.get("/bin/cpc/buildreport", { 'start': start, 'end': end, 'csv': csv, 'type': type }, function(data) {
		  var results = $('#results');
		  
		  $('#select-all').off();
		  
		  results.empty();
		  if(csv) {
			  var a         = document.createElement('a');
			  a.href        = 'data:attachment/csv,' +  encodeURIComponent(data);
			  a.target      = '_blank';
			  a.download    = 'report.csv';

			  document.body.appendChild(a);
			  a.click();
			  $(a).remove();
		  } else {
			  var table = 	"<coral-table>" +
			  				"	<table is='coral-table-inner'>" +
			  				"		<thead is='coral-thead'>" +
			  				"			<tr is='coral-tr'>" +
						  	"				<th is='coral-th'><coral-checkbox id='select-all'/></th>";

			  for(var i = 0; i < data.headers.length; i++) {
				  table += "<th is='coral-th'>" + data.headers[i] + "</th>";
			  }

			  table += "<th is='coral-th'>Debug</th>"
			  table += "</tr></thead><tbody is='coral-tbody'>";

			  for(var i = 0; i < data.results.length; i++) {
				  table +=	"<tr is='coral-tr'>" +
				  			"	<td is='coral-td'>" +
				  			"		<coral-checkbox class='select-one'/>" + 
				  			"   </td>" +
		  					"	<td is='coral-td'><a href='" + data.results[i].path + ".html'>" + data.results[i].path + "</a></td>" +
		  					"	<td is='coral-td'>" + data.results[i].columnb + "</td>";
				  
				  if(data.headers.length > 2) {
					  table += "<td is='coral-td'>" + data.results[i].columnc + "</td>";
				  }
		  					
				  table += 	"	<td is='coral-td'>" +
		  					" 		<button class='debug-content' is='coral-button' variant='minimal' icon='globe' iconsize='S'/>" + 
		  					"	</td>" +
		  					"</tr>";
			  }

			  table += '</tbody></table></coral-table>';
			  results.append(table);
		  }
		  
		  $('#wait-overlay').css('display', 'none');
		  
		  /**
		   * SELECT ALL LISTENER FOR REPORT MODAL
		   * 
		   * This listener is added here as opposed to document.ready because it won't bind until there's results
		   */
		  $('#select-all').on('change', function(event) {
			  var checked = event.currentTarget.checked;
			  $('.select-one').each(function() {
		      	$(this).prop('checked', checked);
		      	if(checked)
		      		checkedCount += 1;
		      	else
		      		checkedCount -= 1;
		      	
		      	if(checkedCount >= 1) {
		      		$activateSelectedBtn.attr('disabled', false);
		      		$unlockSelectedBtn.attr('disabled', false);
		      	} else {
		      		$activateSelectedBtn.attr('disabled', true);
		      		$unlockSelectedBtn.attr('disabled', true);
		      	}
		      });
		  });
		  
		  /**
		   * SELECT ONE ROW LISTENER FOR REPORT MODAL
		   * 
		   * This listener is added here as opposed to document.ready because it won't bind until there's results
		   */
		  $('.select-one').on('change', function(event) {
			  var checked = event.currentTarget.checked;
			  if(checked)
				  checkedCount += 1;
			  else
				  checkedCount -= 1;
			  
			  if(checkedCount >= 1) {
				  $activateSelectedBtn.attr('disabled', false);
				  $unlockSelectedBtn.attr('disabled', false);
			  } else {
				  $activateSelectedBtn.attr('disabled', true);
				  $unlockSelectedBtn.attr('disabled', true);
			  }
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
			  var publishUrl = $('#publishers').val();
			  var text = $('#publishers')[0].selectedItem;
			  var path = $(this).closest('tr').children().eq(1).find('a').eq(0).attr('href');

			  if(null != text) {
				  text = text.textContent;
			  }
			  
			  if(null != publishUrl && null != text) {
				  text = $('#publishers')[0].selectedItem.textContent;
				  
				  var $flushAgents = $(".agent[data-agent='" + text +"'] .delete-cache-btn");
				  
				  window.open(path, '_blank'); 					// Open on author (only need path because if we're staying on same instance, AEM will handle the rest
				  window.open(publishUrl + path, '_blank'); 	// Open on chosen publish instance (need full URL since we're leaving the instance)
				  
				  // For this publish instance, we may have multiple dispatchers so let's loop through them
				  $.each($flushAgents, function(idx, val) {
					  var transportUri = $(val).attr('data-transporturi');
					  window.open(transportUri.substring(0, transportUri.indexOf('/dispatcher')) + path, '_blank');
				  });
			  } else {
				  alert('Please select a publish instance to debug content');
			  }
		  });
	  });
  });

  /**
   * STATE VALIDATION FOR STARTDATE AND ENDDATE FIELDS BASED ON SELECTED REPORT TYPE
   * 
   * If our chosen report type is "locked" then we don't need to show the date fields
   */
  $('#report-type').on('change', function(event) {
	  var typeParam = $('#report-type').val();
	  var startParam = $('#startdate').val();
	  var endParam = $('#enddate').val();
	  
	  if(typeParam == 'Locked' || typeParam == '') {
		  $('#startdate').hide();
		  $('#enddate').hide();
		  $('#query-btn, #query-to-csv-btn').prop('disabled', false);
	  } else {
		  $('#startdate').show();
		  $('#enddate').show();
		  if('' != startParam && '' != endParam) {
			  $('#query-btn, #query-to-csv-btn').prop('disabled', false);
		  } else {
			  $('#query-btn, #query-to-csv-btn').prop('disabled', true);
		  }
	  }
  });
  
  /**
   * STATE VALIDATION FOR THE QUERY AND QUERY TO CSV BUTTONS FOR DATE RANGE REPORTS
   * 
   * Only enable the Query and Query to CSV buttons once we have a valid date range
   * for reports by date range
   */
  $('#startdate, #enddate').on('change', function(event) {
	  var startParam = $('#startdate').val();
	  var endParam = $('#enddate').val();

	  if('' != startParam && '' != endParam) {
		  $('#query-btn, #query-to-csv-btn').prop('disabled', false);
	  } else {
		  $('#query-btn, #query-to-csv-btn').prop('disabled', true);
	  }
  });
  
  /**
   * ACTIVATE SELECTED ROWS LISTENER FOR REPORT MODAL
   * 
   * For each selected row's content path, performs replication
   */
  $('#activate-selected-btn').on('click', function(event) {
	  var $selectedInputs = $('#results .select-one > input:checked');
	  var pathsToReplicate = '';
	  
	  $selectedInputs.each(function() {
		  pathsToReplicate += $(this).closest('td').next().text() + ',';
	  });
	  	
	  $.post('/bin/cpc/activateselected', { 'paths': pathsToReplicate.slice(0, -1) }, function(data) {
		  alert(data);
	  });
  });
  
  /**
   * UNLOCK SELECTED ROWS LISTENER FOR REPORT MODAL
   * 
   * For each selected row's content path, unlocks it
   */
  $('#unlock-selected-btn').on('click', function(event) {
	  var $selectedInputs = $('#results .select-one > input:checked');
	  var pathsToUnlock = '';
	  
	  $selectedInputs.each(function() {
		  pathsToUnlock += $(this).closest('td').next().text() + ',';
	  });
	  	
	  $.post('/bin/cpc/unlockselected', { 'paths': pathsToUnlock.slice(0, -1) }, function(data) {
		  alert(data);
	  });
  });

  refreshQueues();
})