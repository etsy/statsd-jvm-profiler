This directory contains utilities for visualizing the output of the profiler.

## graphite_dump.py

This script will dump the output of the profiler from Graphite and format it in a manner suitable for use with [FlameGraph](https://github.com/brendangregg/FlameGraph).  You must specify the Graphite host, the prefix of the metrics, and the start and end date for the period to be visualized.

Some example flame graphs are in the `example_flame_graphs` directory.