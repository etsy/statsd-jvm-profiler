var path = require('path')
var spawn = require('child_process').spawnSync

exports.getFlameGraph = function(metrics, callback) {
    if (Object.keys(metrics).length === 0) {
	callback('Unable to retrieve CPU metrics');
	return;
    }
    var collapsedMetrics = metrics.filter(function(metric) {
	// This filters out the metrics representing the upper and lower bounds on depth of the metric hierarchy
	return metric.metric != ':' + metric.value;
    }).map(function(metric) {
	return metric.metric + " " + Math.round(metric.value);
    });
    var flamegraphScriptPath = path.join(__dirname, 'flamegraph.pl')
    var child = spawn(flamegraphScriptPath, ['--title', 'Flame Graph', '--width', '1800'],  {'input': collapsedMetrics.join('\n')});
    callback(child.stdout)
}
