This directory contains utilities for visualizing the output of the profiler.  Some example flame graphs are in the `example_flame_graphs` directory.

## influxdb-dashboard

This is a simple dashboard for visualizing the metrics produced by the profiler using the InfluxDB backend.  See the README in this directory for more information.

## graphite_dump.py

This script will dump the output of the profiler from Graphite and format it in a manner suitable for use with [FlameGraph](https://github.com/brendangregg/FlameGraph).  You must specify the Graphite host, the prefix of the metrics, and the start and end date for the period to be visualized.

### Usage
graphite_dump.py takes the following options:

Option | Meaning
-------|--------
-o     | Hostname where Graphite is running
-s     | Beginning of the time range to dump in the HH:MM_yyyymmdd format
-e     | End of the time range to dump in the HH:MM_yyyymmdd format
-p     | Prefix for the metrics to dump


An example invocation would be
```
graphite_dump.py -o graphitehost -s 19:48_20141230 -e 19:50_20141230 -p statsd-jvm-profiler.cpu.trace
```

## influxdb_dump.py

This script will dump the output of the profiler from InfluxDB format it in a manner suitable for use with [FlameGraph](https://github.com/brendangregg/FlameGraph).

It requires [influxdb-python](https://github.com/influxdb/influxdb-python) to be installed.

### Usage
influxdb_dump.py takes the following options:

Option | Meaning
-------|--------
-o     | Hostname where InfluxDB is running (required)
-r     | Port for the InfluxDB HTTP API (optional, defaults to 8086)
-u     | Username to use when connecting to InfluxDB (required)
-p     | Password to use when connection to InfluxDB (required)
-d     | Database containing the profiler metrics (required)
-e     | Prefix of metrics. This would be the same value as the `prefix` argument given to the profiler (required)
-t     | Tag mapping for metrics.  This would be the same value as the `tagMapping` argument given to the profiler (optional, defaults to none).

An example invocation would be:
```
influxdb_dump.py -o influxdbhost -u profiler -p password -d profiler -e bigdata.profiler.ajohnson.job1.flow1.stage1.phase1 -t SKIP.SKIP.username.job.flow.stage.phase
```
