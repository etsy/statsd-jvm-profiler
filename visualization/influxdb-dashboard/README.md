# statsd-jvm-profiler-dash
A dashboard to visualize the metrics produced by
[statsd-jvm-profiler](https://github.com/etsy/statsd-jvm-profiler).

This dashboard only supports the InfluxDB backend for the profiler.

## Configuration
Configuration is done with the dashboard-config.json file in the
project root.  An example (example-dashboard-config.json) is provided.
You must provide values for all the keys in that example, which
includes the information necessary to connect to InfluxDB and the base
prefix for metrics.

The `/config` endpoint will return the current configuration.

## Metric Structure
This dashboard assumes a particular metric prefix structure: `<base
prefix>.<username>.<job>.<run id>.<stage number>.<phase>`.  The
prefix configured when profiling with statsd-jvm-profiler must match
this structure.  The [example FlowListener](https://github.com/etsy/statsd-jvm-profiler/blob/master/example/StatsDProfilerFlowListener.scala) 
produces metrics in this format.  `<base prefix>` is configured in
dashboard-config.json, but the available values for the others are
automatically pulled from the metrics that exist in the configured
InfluxDB database.
 

## Installation
It is assumed that you already have InfluxDB configured and running
before setting up this dashboard.

1. Clone repository.
2. Open a command prompt, navigate to the folder, and enter: npm install
3. Next, run the app by entering: nodejs app.js
4. Browse to http://localhost:3888

## Usage
The homepage allows selecting the username, job, run id, stage number,
and phase (map or reduce) from the available values.  Clicking the
"Render" button will display visualizations of memory usage and GC
statistics.  A flame graph can also be generated from this page.

## Project Template
Kory Becker http://www.primaryobjects.com/kory-becker.aspx
