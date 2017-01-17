/**
 * Clears the cache for a single dispatcher
 */
function clearCache(toggle) {
	  $.ajax({
		  url: toggle.dataset.transporturi,
		  type: 'post',
		  headers: {
			  'CQ-Action': 'Activate',   
			  'CQ-Handle': '/',
			  'Content-length': '0'
		  },
		  dataType: 'json',
		  success: function (data) {
	        console.info('Cache successfully cleared: ' + data);
		  }
	  });
}
/**
 * Sets the status light for an individual agent
 */
function setQueueStatus($agent, data) {
	var blocked = data ? data.metaData.queueStatus.isBlocked : false;
	var unpaused = $agent.find('input')[0].checked;
	var $status = $agent.find('.led-box div');

	$status.removeClass();
	if(blocked && unpaused) {
		$status.addClass('led-red');
	} else if(!blocked && unpaused && data.queue.length > 0) {
		$status.addClass('led-blue');
	} else if(!blocked && unpaused) {
		$status.addClass('led-green');
	} else if(!unpaused) {
		$status.addClass('led-yellow');
	}
}

/**
 * Refreshes the queue for an individual agent
 */
function refreshQueue($agent) {
	$.getJSON('/etc/replication/agents.author/' + $agent.find('.agent-id')[0].innerText + '/jcr:content.queue.json', function(data) {
		var $agentTemp = $("[data-agent='" + data.metaData.queueStatus.agentId + "']");
		var $queue = $agentTemp.find('.agent-queue').eq(0);

		setQueueStatus($agentTemp, data);
		$queue.empty();

		if(data.queue.length > 0) {
			for(var j = 0; j < data.queue.length; j++) {
				$queue.append("<a target='_blank' href='" + data.queue[j].path + ".html'>" + data.queue[j].path + "</a>" + "<span>" + data.queue[j].type + "</span>");
			}
		}
	});
}

/**
 * Wrapper that refreshes each for all agents
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
   * INDIVIDUAL AGENT QUEUE PAUSING/UNPAUSING
   */
  $('.agent-toggle').on('click', function(event) {
	  var toggle = event.currentTarget;
	  var id = toggle.getAttribute('data-id');
	  var checked = toggle.getElementsByTagName('input')[0].checked;
	  var $agent = $(toggle.parentElement.parentElement);

	  $.post("/bin/cpc/updateagent", { 'id': id, 'pause': !checked }, function(data) {
		  refreshQueue($agent);
	  });
  });

  /**
   * GROUP AGENT QUEUE PAUSING/UNPAUSING
   */
  $('#pause-group-btn, #unpause-group-btn').on('click', function(event) {
	  var groupToggle = event.currentTarget;
	  var individualToggles = groupToggle.parentElement.parentElement.parentElement.querySelectorAll('.agent-toggle > input');
	  var temp = (groupToggle.textContent || groupToggle.innerText).trim();
	  var pause = temp.indexOf('Pause') >= 0 ? true : false;

	  for(var i = 0; i < individualToggles.length; i++) {
		  var toggle = individualToggles[i];
		  var parent = individualToggles[i].parentElement;
		  var $agent = $(parent.parentElement.parentElement);

		  toggle.checked = !pause;
		  
		  $.post("/bin/cpc/updateagent", { 'id': parent.getAttribute('data-id'), 'pause': pause }, function(data) {
			  if(data.agentId && data.agentId !== '') {
				  refreshQueue($("div[data-agent='" + data.agentId +"']").eq(0));
			  }
		  });
	  }
  });

  /**
   * INDIVIDUAL CACHE INVALIDATION
   */
  $('.clear-cache-btn').on('click', function(event) {
	  clearCache(event.currentTarget);
  });

  /**
   * GROUP CACHE INVALIDATION
   */
  $('#clear-group-cache-btn').on('click', function(event) {
	  var groupToggle = event.currentTarget;
	  var individualToggles = groupToggle.parentElement.parentElement.parentElement.querySelectorAll('.flush-agent');

	  for(var i = 0; i < individualToggles.length; i++) {
		  clearCache(individualToggles[i]);  
	  }
  });
  
  /**
   * VIEW AGENT LOG BUTTON
   */
  $('.view-log-btn').on('click', function(event) {
	  var $this = $(this);
	  
	  $.get('/bin/cpc/viewagentlog', { id: $(this).parent().parent().attr('data-agent') }, function(data) {
		  var $well = $this.parent().parent().find('.agent-queue').first();
		  
		  $well.empty();
		  $well.append(data);
	  });
  });
  
  $('.view-queue-btn').on('click', function(event) {
	  refreshQueue($(this).parent().parent());
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
		  					"	<td class='coral-Table-cell'><a href='" + data.results[i].path + ".html'>" + data.results[i].path + "</a></td>" +
		  					"	<td class='coral-Table-cell'>" + data.results[i].columnb + "</td>" +
		  					"	<td class='coral-Table-cell'>" + data.results[i].columnc + "</td>" +
		  					"</tr>";
			  }
			  
			  table += '</tbody></table>';
			  
			  results.append(table);
		  }
		  
		  /**
		   * SELECT ALL LISTENER FOR REPORT MODAL
		   * 
		   * This listener is added here as opposed to document.ready because it won't bind until there's results
		   */
		  $('#select-all').on('click', function(event) {
			  var checked = $(this).is(':checked');
			  $('.select-one').each(function() {
		      	$(this).attr('checked', checked);
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
		  console.log('Replication has finished with the following data object returned: ' + data);
	  });
  });

  refreshQueues();
})