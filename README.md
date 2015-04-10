# statsd-jvm-profiler [![Build Status](https://travis-ci.org/etsy/statsd-jvm-profiler.svg)](https://travis-ci.org/etsy/statsd-jvm-profiler)

statsd-jvm-profiler is a JVM agent profiler that sends profiling data to StatsD.  Inspired by [riemann-jvm-profiler](https://github.com/riemann/riemann-jvm-profiler), it was primarily built for profiling Hadoop jobs, but can be used with any JVM process.

## Installation

You will need the statsd-jvm-profiler JAR on the machine where the JVM will be running.  If you are profiling Hadoop jobs, that means the JAR will need to be on all of the datanodes.

The JAR can be built with `mvn package`.  You will need a relatively recent Maven (at least Maven 3).

## Usage

The profiler is enabled using the JVM's `-javaagent` argument.  You are required to specify at least the StatsD host and port number to use.  You can also specify the prefix for metrics and a whitelist of packages to be included in the CPU profiling.  Arguments can be specified like so:
```
-javaagent:/usr/etsy/statsd-jvm-profiler/statsd-jvm-profiler.jar=server=hostname,port=num
```

An example of setting up Cascading/Scalding jobs to use the profiler can be found in the `example` directory.

### Global Options

Name             | Meaning
---------------- | -------
server           | The hostname to which the reporter should send data (required)
port             | The port number for the server to which the reporter should send data (required)
prefix           | The prefix for metrics (optional, defaults to statsd-jvm-profiler)
packageWhitelist | Colon-delimited whitelist for packages to include (optional, defaults to include everything)
packageBlacklist | Colon-delimited whitelist for packages to exclude (optional, defaults to exclude nothing)
profilers        | Colon-delimited list of profiler class names (optional, defaults to CPUProfiler and MemoryProfiler)
reporter         | Class name of the reporter to use (optional, defaults to StatsDReporter)

### Reporters
statsd-jvm-profiler supports multiple backends.  StatsD is the default, but InfluxDB is also supported.  You can select the backend to use by passing the `reporter` argument to the profiler; `StatsDReporter` and `InfluxDBReporter` are the supported values.

Some reporters may require additional arguments.

#### StatsDReporter
This reporter does not have any additional arguments.

#### InfluxDBReporter

Name        | Meaning
----------- | -------
username    | The username with which to connect to InfluxDB (required)
password    | The password with which to connect to InfluxDB (required)
database    | The database to which to write metrics (required)

## Metrics

`statsd-jvm-profiler` will profile the following:

1. Heap and non-heap memory usage
2. Number of GC pauses and GC time
3. Time spent in each function

Assuming you use the default prefix of `statsd-jvm-profiler`, the memory usage metrics will be under `statsd-jvm-profiler.heap` and `statsd-jvm-profiler.nonheap`, the GC metrics will be under `statsd-jvm-profiler.gc`, and the CPU time metrics will be under `statsd-jvm-profiler.cpu.trace`.

Memory and GC metrics are reported once every 10 seconds.  The CPU time is sampled every millisecond, but only reported every 10 seconds.  The CPU time metrics represent the total time spent in that function.

Profiling a long-running process or a lot of processes simultaneously will produce a lot of data, so be careful with the capacity of your StatsD instance.  The `packageWhitelist` and `packageBlacklist` arguments can be used to limit the number of functions that are reported.  Any function whose stack trace contains a function in one of the whitelisted packages will be included.

You can disable either the memory or CPU metrics using the `profilers` argument:

1. Memory metrics only: `profilers=MemoryProfiler`
2. CPU metrics only: `profilers=CPUProfiler`

## Visualization

The `visualization` directory contains some utilities for visualizing the output of the profiler.
