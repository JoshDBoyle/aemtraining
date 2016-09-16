$(document).ready(function() {
  var agentsInfoModal = new CUI.Modal({
    element : '#agents-info-modal',
    visible : false
  });

  $('#agents-info-button').on('click', function(event) {
	  agentsInfoModal.show();
  });
  
  $('.agent-toggle').on('click', function(event) {
	  var toggle = event.currentTarget;
	  var id = toggle.getAttribute('data-id');

	  $.post("/bin/cpc/updateagent", { 'id': id, 'enabled': toggle.getElementsByTagName('input')[0].checked }, function(data) {
		  console.log("Agent updated");
	  });
  });
})