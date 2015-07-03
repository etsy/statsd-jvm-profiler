var influx = require('influx')
var config = require('./config.js')

var client = null;
config.getConfig(function(conf) {
    client = influx(conf['influxdb']);
});

// This measurement will always exist and can be used to query for valid tags
var canaryMeasurement = "heap.total.max";

exports.getData = function(user, job, flow, stage, phase, metric, callback) {
    var query = "select value from /^" + metric + ".*/ where username = '" + user + "' and job = '" + job + "' and flow = '" + flow + "' and stage = '" + stage + "' and phase = '" + phase + "'";
    client.queryRaw(query, function(err, res) {
	var series = res[0].series;
	var results = series.map(function(series) {
	    var name = series.name.replace(metric + '.', '');
	    var points = series.values.map(function(value) {
		return {time: new Date(value[0]).getTime(), value: value[1]};
	    });

	    return {metric: name, values: points};
	});
	callback(results);
    });
}


exports.getFlameGraphData = function(user, job, flow, stage, phase,  prefix, callback) {
    var query = "select value from /^" + prefix + ".*/ where username = '" + user + "' and job = '" + job + "' and flow = '" + flow + "' and stage = '" + stage + "' and phase = '" + phase + "'";
    client.queryRaw(query, function(err, res) {
	var series = res[0].series;
    	var results = series.map(function(series) {
    	    var name = formatMetric(series.name, prefix);
    	    var value = Math.max.apply(null, series.values.map(function(value) {
    		return value[1];
    	    }));

    	    return {metric: name, value: value};
    	});
    	callback(results);
    });
}

exports.getOptions = function(prefix, callback) {
    client.getSeries("heap.total.max", function(err, seriesNames) {
	var result = {};
	var series = seriesNames[0];
	var columns = series.columns;

	var userIndex = columns.indexOf('username');
	var jobIndex = columns.indexOf('job');
	var flowIndex = columns.indexOf('flow');
	var stageIndex = columns.indexOf('stage');
	var phaseIndex = columns.indexOf('phase');
	
	series.values.map(function(values) {
	    var user = values[userIndex];
	    var job = values[jobIndex];
	    var flow = values[flowIndex];
	    var stage = values[stageIndex];
	    var phase = values[phaseIndex];

	    var userVal = result[user]
    	    if (!userVal) {
    		userVal = {};
    	    }

    	    var jobVal = userVal[job];
    	    if (!jobVal) {
    		jobVal = {};
    	    }

    	    var flowVal = jobVal[flow];
    	    if(!flowVal) {
    		flowVal = {};
    	    }

    	    var stageVal = flowVal[stage];
    	    if(!stageVal) {
    		stageVal = [];
    	    }

    	    if(stageVal.indexOf(phase) == -1) {
    		stageVal.push(phase);
    	    }
	    
    	    flowVal[stage] = stageVal;
    	    jobVal[flow] = flowVal;
    	    userVal[job] = jobVal;
    	    result[user] = userVal;
	});
	callback(result);
    });
}

function formatMetric(metric, prefix) {
    var noPrefix = metric.replace(prefix, '');
    var frames = noPrefix.split('.');
    frames.splice(0, 1);
    frames.reverse();
    var lineNumFrames = frames.map(function(frame) {
	var lineNumSepIndex = frame.lastIndexOf('-');
	return frame.substring(0, lineNumSepIndex) + ':' + frame.substring(lineNumSepIndex+1);
    });

    return lineNumFrames.join(',').replace(/-/g, '.');
}
