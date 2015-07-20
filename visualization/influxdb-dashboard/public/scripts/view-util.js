var ViewUtil = (function($) {
    var view = {};

    view.getMetricsForPool = function(pool, metrics) {
	return metrics.map(function(metric) {
	    return {metric: pool + '.' + metric.metric, alias: metric.alias };
	});
    }
    
    view.renderSelect = function(id, users) {
	var html = '<select>';
	for (i = 0; i < users.length; ++i) {
	    html += '<option value=' + users[i] + '>' + users[i] + '</option>';
	}
	html += '</select>';
	$(id).hide().html(html).show();
    }

    view.renderGraph = function(results, title, div, metrics) {
	var data = metrics.map(function(metric) {
	    var values = $.grep(results, function(e){ return e.metric == metric.metric; })[0]['values'];
	    return [[metric.metric].concat(values.map(function(value) {
		return value.time;
	    })),
		    [metric.alias].concat(values.map(function(value) {
			return value.value;
		    }))];
	});
	
	var columns = [].concat.apply([], data);
	var xs = {};
	metrics.map(function(m) {
	    xs[m.alias] = m.metric;
	});
	c3.generate({
	    bindto: div,
	    data: {
		xs: xs,
		columns: columns
	    },
	    axis: {
		y: {
		    tick: {
			format: d3.format('s')
		    }
		}
	    },
	    zoom: {
		enabled: true
	    }
	});
    }

    return view;
    
}(jQuery));
