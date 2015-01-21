This directory contains utilities for visualizing the output of the profiler.

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

Some example flame graphs are in the `example_flame_graphs` directory.