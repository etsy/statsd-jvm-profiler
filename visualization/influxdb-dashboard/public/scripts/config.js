var fs = require('fs');

exports.getConfig = function(callback) {
    fs.readFile('dashboard-config.json', 'UTF-8', function(err, data) {
	if (err) {
	    console.log(err);
	}

	callback(JSON.parse(data));
    });

}
