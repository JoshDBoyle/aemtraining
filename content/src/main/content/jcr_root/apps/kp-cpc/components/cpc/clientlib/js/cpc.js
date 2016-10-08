/**
 * Sets the status light for an individual agent
 */
function setQueueStatus($agent, data) {
	var blocked = data ? data.metaData.queueStatus.isBlocked : false;
	var enabled = $agent.find('input')[0].checked;
	var $status = $agent.find('.led-box div');

	$status.removeClass();
	if(blocked && enabled) {
		$status.addClass('led-red');
	} else if(!blocked && enabled && data.queue.length > 0) {
		$status.addClass('led-blue');
	} else if(!blocked && enabled) {
		$status.addClass('led-green');
	} else if(!enabled) {
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
				$queue.append("<a href=\"" + data.queue[j].path + "\">" + data.queue[j].path + "</a>" + "<span>" + data.queue[j].type + "</span>");
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

$(document).ready(function() {
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
   * INDIVIDUAL AGENT TOGGLING
   */
  $('.agent-toggle').on('click', function(event) {
	  var toggle = event.currentTarget;
	  var id = toggle.getAttribute('data-id');
	  var checked = toggle.getElementsByTagName('input')[0].checked;
	  var $agent = $(toggle.parentElement.parentElement);

	  $.post("/bin/cpc/updateagent", { 'id': id, 'enabled': checked }, function(data) {
		  refreshQueue($agent);
	  });
  });

  /**
   * GROUP TOGGLING
   */
  $('.group-toggle').on('click', function(event) {
	  var groupToggle = event.currentTarget;
	  var individualToggles = groupToggle.parentElement.parentElement.parentElement.querySelectorAll('.agent-toggle > input');
	  var temp = (groupToggle.textContent || groupToggle.innerText).trim();
	  var enabled = temp.indexOf('Enable') >= 0 ? true : false;

	  for(var i = 0; i < individualToggles.length; i++) {
		  var toggle = individualToggles[i];
		  var parent = individualToggles[i].parentElement;
		  var $agent = $(parent.parentElement.parentElement);

		  toggle.checked = enabled;
		  
		  $.post("/bin/cpc/updateagent", { 'id': parent.getAttribute('data-id'), 'enabled': enabled }, function(data) {
			  if(data.agentId && data.agentId !== '') {
				  refreshQueue($("div[data-agent='" + data.agentId +"']").eq(0));
			  }
		  });
	  }
  });
  
  /**
   * REPORTING OPTIONS
   */
  $('.query-by-date-button, .query-by-date-button-csv').on('click', function(event) {
	  var start = $('#startdate').val();
	  var end = $('#enddate').val();
	  var csv = event.currentTarget.classList.contains('query-by-date-button-csv');
	  var type = $('#report-type').val();

	  $.get("/bin/cpc/querybydate", { 'start': start, 'end': end, 'csv': csv, 'type': type }, function(data) {
		  var results = $('#query-by-date-results');
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
			  var header = "<li>";
			  for(var i = 0; i < data.headers.length; i++) {
				  header += "<h4>" + data.headers[i] + "</h4>";
			  }

			  header += "</li>";
			  results.append(header);

			  for(var i = 0; i < data.results.length; i++) {
				  results.append(	"<li>" +
				  					"	<div><a href='" + data.results[i].path + ".html'>" + data.results[i].path + "</a></div>" +
				  					"	<div>" + data.results[i].columnb + "</div>" +
				  					"	<div>" + data.results[i].columnc + "</div>" +
				  					"</li>");
			  }
		  }
	  });
  });
  
  refreshQueues();
})