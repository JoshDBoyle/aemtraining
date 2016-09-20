$(document).ready(function() {
  var agentsInfoModal = new CUI.Modal({
    element : '#agents-info-modal',
    visible : false
  });
  
  var queryByDateModal = new CUI.Modal({
	 element : '#query-by-date-modal',
	 visible : false
  });

  $('#all-agents-list-button').on('click', function(event) {
	  agentsInfoModal.show();
  });

  $('#query-by-date-button').on('click', function(event) {
	  queryByDateModal.show();
  });
  
  $('.agent-toggle').on('click', function(event) {
	  var toggle = event.currentTarget;
	  var id = toggle.getAttribute('data-id');

	  $.post("/bin/cpc/updateagent", { 'id': id, 'enabled': toggle.getElementsByTagName('input')[0].checked }, function(data) {
		  console.log("Agent " + id + " updated");
	  });
  });

  $('.query-by-date-button').on('click', function(event) {
	  var start = $('#startdate').val();
	  var end = $('#enddate').val();

	  $.get("/bin/cpc/querybydate", { 'startdate': start, 'enddate': end }, function(data) {
		  var results = $('#query-by-date-results');
		  for(var i = 0; i < data.results.length; i++) {
			  results.append(	"<li>" +
			  					"	<span class='path-result'><a href='" + data.results[i].path + ".html'>" + data.results[i].path + "</a></span>" +
			  					"	<span class='last-modified-by-result'>" + data.results[i].lastModifiedBy + "</span>" +
			  					"	<span class='last-modified-result'>" + data.results[i].lastModified + "</span>" +
			  					"</li>");
		  }
	  });
  });

  $('.group-toggle').on('click', function(event) {
	  var groupToggle = event.currentTarget;
	  var individualToggles = groupToggle.parentElement.parentElement.parentElement.querySelectorAll('.agent-toggle > input');
	  var temp = (groupToggle.textContent || groupToggle.innerText).trim();
	  var enabled = temp.indexOf('Enable') >= 0 ? true : false;

	  for(var i = 0; i < individualToggles.length; i++) {
		  var toggle = individualToggles[i];
		  var parent = individualToggles[i].parentElement;

		  toggle.checked = enabled;
		  $.post("/bin/cpc/updateagent", { 'id': parent.getAttribute('data-id'), 'enabled': enabled }, function(data) {
			  console.log("Agent updated");
		  });
	  }
  });
})