var influx = require('../public/scripts/influxdb.js');
var flamegraph = require('../public/scripts/flamegraph.js');
var config = require('../public/scripts/config.js');
var prefix = null;
config.getConfig(function(conf) {
    prefix = conf['prefix'];
})

exports.index = function(req, res){
    res.render('index');
};

exports.render = function(req, res){
    res.render('render', {user: req.query['user'], job: req.query['job'], run: req.query['run'], stage: req.query['stage'], phase: req.query['phase']});
};

exports.config = function(req, res) {
    config.getConfig(function(conf) {
	res.json(conf);
    });
};

exports.options = function(req, res) {
    influx.getOptions(prefix, function(data) { res.json(data) });
};

exports.data = function(req, res) {
    var prefix = req.params['prefix'];
    influx.getData(prefix, function(data) {
	res.json(data);
    });
}

exports.cpu = function(req, res) {
    var prefix = req.params['prefix'];
    influx.getFlameGraphData(prefix, function(metrics) {
	flamegraph.getFlameGraph(metrics, function(data) {
	    res.send(data);
	})
    });
}
