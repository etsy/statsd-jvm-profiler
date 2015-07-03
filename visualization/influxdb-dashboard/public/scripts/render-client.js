$(document).ready(function() {
    $("#toc").toc({
	'highlightOffset': 1,
    });
    var config = {};
    var params = URI(window.location.href).search(true);

    $.ajax({
	async: false,
	type: 'GET',
	url: '/config',
	success: function(data) {
	    config = data;
	}
    });

    var user = params.user;
    var job = params.job;
    var flow = params.flow;
    var stage = params.stage;
    var phase = params.phase;
    var base = params.prefix || config['prefix'] || 'bigdata.profiler';
    var refresh = params.refresh || config['refresh'] || 60;

    var prefix = base + '.' + user + '.' + job + '.' + flow + '.' + stage + '.' + phase;
    var cpuPrefix = 'cpu.trace';
    var heapPrefix = 'heap.total';
    var nonHeapPrefix = 'nonheap.total';
    var finalizePrefix = 'pending-finalization-count';
    var gcPrefix = 'gc';

    var memoryMetrics = [{metric:'init', alias:'Initial'},{metric:'committed', alias:'Committed'},{metric:'max', alias:'Maximum'},{metric:'used', alias:'Used'}];
    var finalizeMetrics = [{metric: finalizePrefix, alias: 'Objects Pending Finalization'}];
    var gcCountMetrics = [{metric:'PS MarkSweep.count', alias:'PS MarkSweep'},{metric:'PS Scavenge.count', alias:'PS Scavenge'}];
    var gcTimeMetrics = [{metric:'PS MarkSweep.time', alias:'PS MarkSweep'},{metric:'PS Scavenge.time', alias:'PS Scavenge'}];
    var gcRuntimeMetrics = [{metric:'PS MarkSweep.runtime', alias:'PS MarkSweep'},{metric:'PS Scavenge.runtime', alias:'PS Scavenge'}];

    $("#toc ul").append('<li class=toc-h2><a href=/cpu/' + user + '/' + job + '/' + flow + '/' + stage + '/' + phase + '/' + cpuPrefix + ' target=_blank>Flame Graph</a></li>');
    $('#toc').affix({
	offset: {
	    top: $('.navbar navbar-default').height()
	}
    });

    var heapGet = $.get('/data/' + user + '/' + job + '/' + flow + '/' + stage + '/' + phase + '/' + heapPrefix);
    var nonHeapGet = $.get('/data/' + user + '/' + job + '/' + flow + '/' + stage + '/' + phase + '/' + nonHeapPrefix);
    var finalizeGet = $.get('/data/' + user + '/' + job + '/' + flow + '/' + stage + '/' + phase + '/' + finalizePrefix);
    var gcGet = $.get('/data/' + user + '/' + job + '/' + flow + '/' + stage + '/' + phase + '/' + gcPrefix);

    $.when(heapGet, nonHeapGet, finalizeGet, gcGet).done(function() {
	var heapResults = heapGet['responseJSON'];
	var nonHeapResults = nonHeapGet['responseJSON'];
	var finalizeResults = finalizeGet['responseJSON'];
	var gcResults = gcGet['responseJSON'];

	ViewUtil.renderGraph(heapResults, 'Heap Usage', '#heap', memoryMetrics);
	ViewUtil.renderGraph(nonHeapResults, 'Non-Heap Usage', '#nonheap', memoryMetrics);
	ViewUtil.renderGraph(finalizeResults, 'Objects Pending Finalization', '#finalize', finalizeMetrics);
	ViewUtil.renderGraph(gcResults, 'Garbage Collection', '#count', gcCountMetrics);
	ViewUtil.renderGraph(gcResults, 'Garbage Collection', '#time', gcTimeMetrics);
	ViewUtil.renderGraph(gcResults, 'Garbage Collection', '#runtime', gcRuntimeMetrics);

    });

    if (refresh > 0) {
	setTimeout(function() {
	    location.reload();
	}, refresh * 1000);
    }
});
