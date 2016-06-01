var influx = require('influx')
var config = require('./config.js')

var client = null;
config.getConfig(function(conf) {
    client = influx(conf['influxdb']);
});

// This measurement will always exist and can be used to query for valid tags
var canaryMeasurement = "heap.total.max";

exports.getData = function(user, job, flow, stage, phase, jvmName, metric, callback) {
    var optionalJvmName = jvmName ? "' and jvmName = '" + jvmName + "'": "'";
    var query = "select value from /^" + metric + ".*/ where username = '" + user + "' and job = '" + job + "' and flow = '" + flow + "' and stage = '" + stage + "' and phase = '" + phase + optionalJvmName;
    client.queryRaw(query, function(err, res) {
	var series = res[0].series;
	var results = {};
	if (series) {
	    results = series.map(function(series) {
		var name = series.name.replace(metric + '.', '');
		var points = series.values.map(function(value) {
		    return {time: new Date(value[0]).getTime(), value: value[1]};
		});

		return {metric: name, values: points};
	    });
	}
	callback(results);
    });
}


exports.getFlameGraphData = function(user, job, flow, stage, phase, jvmName, prefix, callback) {
    var optionalJvmName = jvmName ? "' and jvmName = '" + jvmName + "'": "'";
    var query = "select value from /^" + prefix + ".*/ where username = '" + user + "' and job = '" + job + "' and flow = '" + flow + "' and stage = '" + stage + "' and phase = '" + phase + optionalJvmName;
    client.queryRaw(query, function(err, res) {
	var series = res[0].series;
	var results = {};
	if (series) {
    	    results = series.map(function(series) {
    		var name = formatMetric(series.name, prefix);
    		var value = (null, series.values.map(function(value) {
    		    return value[1];
    		})).reduce(function(total, curr) {
		    return total + curr;
		}, 0);

    		return {metric: name, value: value};
    	    });
	}
    	callback(results);
    });
}

exports.getOptions = function(prefix, callback) {
    client.getSeries("heap.total.max", function(err, seriesNames) {
	var result = {};
	if (seriesNames !== undefined) {
	    var series = seriesNames[0]
	    var columns = series.values.map(function(value) {
		var tokens = value[0].split(',');
		column = {}
		tokens.forEach(function(token) {
		    if (token.indexOf("=") != -1) {
			var split = token.split("=");
			column[split[0]] = split[1];
		    }
		})
		return column;
	    });
	    
	    columns.map(function(values) {
		var user = values['username'];
		var job = values['job'];
		var flow = values['flow'];
		var stage = values['stage'];
		var phase = values['phase'];
		var jvmName = values['jvmName'];

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
    		    stageVal = {};
    		}


		var phaseVal = stageVal[phase];
    		if(!phaseVal) {
    		    phaseVal = [];
    		}

		if (phaseVal.indexOf(jvmName) == -1) {
		    phaseVal.push(jvmName);
		}

		stageVal[phase] = phaseVal;
    		flowVal[stage] = stageVal;
    		jobVal[flow] = flowVal;
    		userVal[job] = jobVal;
    		result[user] = userVal;
	    });
	    callback(result);
	}
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

    return lineNumFrames.join(';').replace(/-/g, '.');
}
