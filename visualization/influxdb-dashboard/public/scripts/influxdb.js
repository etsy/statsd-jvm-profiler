var influx = require('influx')
var config = require('./config.js')

var client = null;
config.getConfig(function(conf) {
    client = influx(conf['influxdb']);
})

exports.getData = function(prefix, callback) {
    var query = "select * from /" + prefix + ".*/;";
    client.query(query, function(err, res) {
	var results = res.map(function(metric) {
	    var name = metric.name.replace(prefix + '.', '');
	    var points = metric.points.map(function(point) {
		return {time: point[0], value: point[2]};
	    })
	    return {metric: name, values: points};
	})
	callback(results);
    });
}

exports.getFlameGraphData = function(prefix, callback) {
    var query = "select * from /" + prefix + ".*/;";
    client.query(query, function(err, res) {
	var results = res.map(function(metric) {
	    var name = formatMetric(metric.name, prefix);
	    var value = Math.max.apply(null, metric.points.map(function(point) {
		return point[2];
	    }));

	    return {metric: name, value: value};
	});
	callback(results);
    });
}

exports.getOptions = function(prefix, callback) {
    client.query("list series", function(err, res) {
	var result = {};
	res[0]['points'].map(function(point) {
	    var name = point[1];
	    var tokens = name.split(".")
	    var user = tokens[2];
	    var job = tokens[3];
	    var run = tokens[4];
	    var stage = tokens[5];
	    var phase = tokens[6];

	    var userVal = result[user]
	    if (!userVal) {
		userVal = {};
	    }

	    var jobVal = userVal[job];
	    if (!jobVal) {
		jobVal = {};
	    }

	    var runVal = jobVal[run];
	    if(!runVal) {
		runVal = {};
	    }

	    var stageVal = runVal[stage];
	    if(!stageVal) {
		stageVal = [];
	    }

	    if(stageVal.indexOf(phase) == -1) {
		stageVal.push(phase);
	    }
	    
	    runVal[stage] = stageVal;
	    jobVal[run] = runVal;
	    userVal[job] = jobVal;
	    result[user] = userVal;
	})
	callback(result);
    });
}

function formatMetric(metric, prefix) {
    var noPrefix = metric.replace(prefix, '');
    var frames = noPrefix.split('.');
    frames.splice(0, 1);
    frames.reverse();
    return frames.join(',').replace('-', '.');
}
