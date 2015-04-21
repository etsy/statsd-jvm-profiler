var stackvis = require('stackvis');
var bunyan = require('bunyan');
var memorystream = require('memory-streams');
var resumer = require('resumer');

exports.getFlameGraph = function(metrics, callback) {
    var reader = stackvis.readerLookup('collapsed');
    var writer = stackvis.writerLookup('flamegraph-d3');
    var collapsedMetrics = metrics.filter(function(metric) {
	// This filters out the metrics representing the upper and lower bounds on depth of the metric hierarchy
	return metric.metric != metric.value;
    }).map(function(metric) {
	return metric.metric + " " + Math.round(metric.value);
    });

    // We must use resumer here instead of memory-streams so the
    // "end" event is emitted properly
    var metricStream = resumer().queue(collapsedMetrics.join('\n')).end();
    metricStream.setEncoding = function(enc) { };
    var outputStream = new memorystream.WritableStream();
    var log = new bunyan({'name': 'flamegraph', 'stream': outputStream});
    stackvis.pipeStacks(log, metricStream, reader, writer, outputStream, function() {
	callback(outputStream.toString());
    });
}
