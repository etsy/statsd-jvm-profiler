var influx = require('../public/scripts/influxdb.js');
var flamegraph = require('../public/scripts/flamegraph.js');
var config = require('../public/scripts/config.js');
var prefix = null;
var refresh = null
config.getConfig(function(conf) {
    prefix = conf['prefix'];
    refresh = conf['refresh'];
})

exports.index = function(req, res){
    res.render('index');
};

exports.render = function(req, res){
    res.render('render', {user: req.query['user'], job: req.query['job'], flow: req.query['flow'], stage: req.query['stage'], phase: req.query['phase'], jvmName: req.query['jvm'], 'prefix': prefix, 'refresh': refresh});
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
    var user = req.params['user'];
    var job = req.params['job'];
    var flow = req.params['flow'];
    var stage = req.params['stage'];
    var phase = req.params['phase'];
    var jvmName = req.params['jvm'];
    var metric = req.params['metric'];
    influx.getData(user, job, flow, stage, phase, jvmName, metric, function(data) {
	res.json(data);
    });
}

exports.cpu = function(req, res) {
    var user = req.params['user'];
    var job = req.params['job'];
    var flow = req.params['flow'];
    var stage = req.params['stage'];
    var phase = req.params['phase'];
    var jvmName = req.params['jvm'];
    var prefix = req.params['prefix'];
    influx.getFlameGraphData(user, job, flow, stage, phase, jvmName, prefix, function(metrics) {
	flamegraph.getFlameGraph(metrics, function(data) {
	    res.setHeader('content-type', 'image/svg+xml');
	    res.send(data);
	})
    });
}
