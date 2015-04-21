$(document).ready(function() {
    var options = null;

    var getJobs = function() {
	var user = $('#users').val();
	ViewUtil.renderSelect('#jobs', Object.keys(options[user]));
	$('#jobs').attr('name', 'job');
	getRuns();
    };

    var getRuns = function() {
	var user = $('#users').val();
	var job = $('#jobs').val();
	ViewUtil.renderSelect('#runs', Object.keys(options[user][job]));
	$('#runs').attr('name', 'run');
	getStages();
    };

    var getStages = function() {
	var user = $('#users').val();
	var job = $('#jobs').val();
	var run = $('#runs').val();
	ViewUtil.renderSelect('#stages', Object.keys(options[user][job][run]));
	$('#stages').attr('name', 'stage');
	getPhases();
    };

    var getPhases = function() {
	var user = $('#users').val();
	var job = $('#jobs').val();
	var run = $('#runs').val();
	var stage = $('#stages').val();
	ViewUtil.renderSelect('#phases', options[user][job][run][stage]);
	$('#phases').attr('name', 'phase');
    }
    
    var refresh = function(o) {
	options = o;
	ViewUtil.renderSelect('#users', Object.keys(options));
	$('#users').attr('name', 'user');

	getJobs();;
    }
    
    $.get('/options', refresh);

    $('#users').change(getJobs);
    $('#jobs').change(getRuns);
    $('#runs').change(getStages);
    $('#stages').change(getPhases);
});
