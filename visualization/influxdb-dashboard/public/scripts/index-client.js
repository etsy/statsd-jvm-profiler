$(document).ready(function() {
    var options = null;

    var getJobs = function() {
	var user = $('#users').val();
	ViewUtil.renderSelect('#jobs', Object.keys(options[user]));
	$('#jobs').attr('name', 'job');
	getFlows();
    };

    var getFlows = function() {
	var user = $('#users').val();
	var job = $('#jobs').val();
	ViewUtil.renderSelect('#flows', Object.keys(options[user][job]));
	$('#flows').attr('name', 'flow');
	getStages();
    };

    var getStages = function() {
	var user = $('#users').val();
	var job = $('#jobs').val();
	var flow = $('#flows').val();
	ViewUtil.renderSelect('#stages', Object.keys(options[user][job][flow]));
	$('#stages').attr('name', 'stage');
	getPhases();
    };

    var getPhases = function() {
	var user = $('#users').val();
	var job = $('#jobs').val();
	var flow = $('#flows').val();
	var stage = $('#stages').val();
	ViewUtil.renderSelect('#phases', Object.keys(options[user][job][flow][stage]));
	$('#phases').attr('name', 'phase');
	getJvms();
    }

    var getJvms = function() {
	var user = $('#users').val();
	var job = $('#jobs').val();
	var flow = $('#flows').val();
	var stage = $('#stages').val();
	var phase = $('#phases').val();
	ViewUtil.renderSelect('#jvms', options[user][job][flow][stage][phase]);
	$('#jvms').attr('name', 'jvm');
    }
    
    var refresh = function(o) {
	options = o;
	ViewUtil.renderSelect('#users', Object.keys(options));
	$('#users').attr('name', 'user');

	getJobs();
    }
    
    $.get('/options', refresh);

    $('#users').change(getJobs);
    $('#jobs').change(getFlows);
    $('#flows').change(getStages);
    $('#stages').change(getPhases);
});
