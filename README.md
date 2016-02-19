# statsd-jvm-profiler [![Build Status](https://travis-ci.org/etsy/statsd-jvm-profiler.svg)](https://travis-ci.org/etsy/statsd-jvm-profiler)

statsd-jvm-profiler is a JVM agent profiler that sends profiling data to StatsD.  Inspired by [riemann-jvm-profiler](https://github.com/riemann/riemann-jvm-profiler), it was primarily built for profiling Hadoop jobs, but can be used with any JVM process.

Read [the blog post](https://codeascraft.com/2015/01/14/introducing-statsd-jvm-profiler-a-jvm-profiler-for-hadoop/) that introduced statsd-jvm-profiler on [Code as Craft](https://codeascraft.com/), Etsy's engineering blog.

Also check out [the blog post](https://codeascraft.com/2015/05/12/four-months-of-statsd-jvm-profiler-a-retrospective/) reflecting on the experience of open-sourcing the project.

## Mailing List
There is a mailing list for this project at https://groups.google.com/forum/#!forum/statsd-jvm-profiler.  If you have questions or suggestions for the project send them here!

## Installation

You will need the statsd-jvm-profiler JAR on the machine where the JVM will be running.  If you are profiling Hadoop jobs, that means the JAR will need to be on all of the datanodes.

The JAR can be built with `mvn package`.  You will need a relatively recent Maven (at least Maven 3).

statsd-jvm-profiler is available in Maven Central:
```xml
<dependency>
  <groupId>com.etsy</groupId>
  <artifactId>statsd-jvm-profiler</artifactId>
  <version>1.0.1</version>
</dependency>
```

If you would like an uberjar containing all of the dependencies instead of the standard JAR, use the `jar-with-dependencies` classifier:
```xml
<dependency>
  <groupId>com.etsy</groupId>
  <artifactId>statsd-jvm-profiler</artifactId>
  <version>1.0.1</version>
  <classifier>jar-with-dependencies</classifier>
</dependency>
```

## Usage

The profiler is enabled using the JVM's `-javaagent` argument.  You are required to specify at least the StatsD host and port number to use.  You can also specify the prefix for metrics and a whitelist of packages to be included in the CPU profiling.  Arguments can be specified like so:
```
-javaagent:/usr/etsy/statsd-jvm-profiler/statsd-jvm-profiler.jar=server=hostname,port=num
```

You should use the uberjar when starting the profiler in this manner so that all the profiler's dependencies are available.

The profiler can also be loaded dynamically (after the JVM has already started), but this technique requires relying on Sun's `tools.jar`, meaning it's an implementation-specific solution that might not work for all JVMs. For more information see the [Dynamic Loading section](#dynamic-loading-of-agent). 

An example of setting up Cascading/Scalding jobs to use the profiler can be found in the `example` directory.

### Global Options

Name             | Meaning
---------------- | -------
server           | The hostname to which the reporter should send data (required)
port             | The port number for the server to which the reporter should send data (required)
prefix           | The prefix for metrics (optional, defaults to statsd-jvm-profiler)
packageWhitelist | Colon-delimited whitelist for packages to include (optional, defaults to include everything)
packageBlacklist | Colon-delimited whitelist for packages to exclude (optional, defaults to exclude nothing)
profilers        | Colon-delimited list of profiler class names (optional, defaults to `CPUTracingProfiler` and `MemoryProfiler`)
reporter         | Class name of the reporter to use (optional, defaults to StatsDReporter)
httpServerEnabled| Determines if the embedded HTTP server should be started. (optional, defaults to `true`)
httpPort         | The port on which to bind the embedded HTTP server (optional, defaults to 5005). If this port is already in use, the next free port will be taken.

### Embedded HTTP Server
statsd-jvm-profiler embeds an HTTP server to support simple interactions with the profiler while it is in operation.
You can configure the port on which this server runs with the `httpPort` option.
You can disable it altogether using the `httpServerEnabled=false` argument.
 
Endpoint                    | Usage
---------------             | -----
/profilers                  | List the currently enabled profilers
/isRunning                  | List the running profilers. This should be the same as /profilers.
/disable/:profiler          | Disable the profiler specified by `:profiler`. The name must match what is returned by `/profilers`.
/errors                     | List the past 10 errors from the running profilers and reporters.
/status/profiler/:profiler  | Displays a status message with the number of recorded stats for the requested profiler.

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
tagMapping  | A mapping of tag names from the metric prefix (optional, defaults to no mapping)

##### Tag Mapping
InfluxDB 0.9 supports tagging measurements and querying based on those tags.  statsd-jvm-profilers uses these tags to support richer querying of the produced data.  For compatibility with other metric backends, the tags are extracted from the metric prefix.

If the `tagMapping` argument is not defined, only the `prefix` tag will be added, with the value of the entire prefix.

`tagMapping` should be a period-delimited set of tag names.  It must have the same number of components as `prefix`, or else an exception would be thrown.  Each component of `tagMapping` is the name of the tag.  The component in the corresponding position of `prefix` will be the value.

If you do not want to include a component of `prefix` as a tag, use the special name `SKIP` in `tagMapping` for that position.

## Profilers

`statsd-jvm-profiler` offers 3 profilers: `MemoryProfiler`, `CPUTracingProfiler` and `CPULoadProfiler`.

The metrics for all these profilers will prefixed with the value from the `prefix` argument or it's default value: `statsd-jvm-profiler`.

You can enable specific profilers through the `profilers` argument like so:
1. Memory metrics only: `profilers=MemoryProfiler`
2. CPU Tracing metrics only: `profilers=CPUTracingProfiler`
3. JVM/System CPU load metrics only: `profilers=CPULoadProfiler`

Default value: `profilers=MemoryProfiler:CPUTracingProfiler`

### Garbage Collector and Memory Profiler: `MemoryProfiler`
This profiler will record:

1. Heap and non-heap memory usage
2. Number of GC pauses and GC time

Assuming you use the default prefix of `statsd-jvm-profiler`,
the memory usage metrics will be under `statsd-jvm-profiler.heap` and `statsd-jvm-profiler.nonheap`,
the GC metrics will be under `statsd-jvm-profiler.gc`.

Memory and GC metrics are reported once every 10 seconds.

### CPU Tracing Profiler: `CPUTracingProfiler`
This profiler records the time spent in each function across all Threads.

Assuming you use the default prefix of `statsd-jvm-profiler`, the the CPU time metrics will be under `statsd-jvm-profiler.cpu.trace`.

The CPU time is sampled every millisecond, but only reported every 10 seconds.
The CPU time metrics represent the total time spent in that function.

Profiling a long-running process or a lot of processes simultaneously will produce a lot of data, so be careful with the
capacity of your StatsD instance.  The `packageWhitelist` and `packageBlacklist` arguments can be used to limit the number
of functions that are reported. Any function whose stack trace contains a function in one of the whitelisted packages will be included.

The `visualization` directory contains some utilities for visualizing the output of this profiler.

### JVM And System CPU Load Profiler: `CPULoadProfiler`

This profiler will record the JVM's and the overall system's CPU load, if the JVM is capable of providing this information.

Assuming you use the default prefix of `statsd-jvm-profiler`, the JVM CPU load metrics will be under `statsd-jvm-profiler.cpu.jvm`,
and the System CPU load wil be under `statsd-jvm-profiler.cpu.system`.

The reported metrics will be percentages in the range of [0, 100] with 1 decimal precision.

CPU load metrics are sampled and reported once every 10 seconds.

Important notes:
* This Profiler is not enabled by default. To enable use the argument `profilers=CPULoadProfiler`
* This Profiler relies on Sun/Oracle-specific JVM implementations that offer a JMX bean that might not be available in other JVMs.
  Even if you are using the right JVM, there's no guarantee this JMX bean will remain there in the future.
* The minimum required JVM version that offers support for this is for Java 7.
* See [com.sun.management.OperatingSystemMXBean](https://docs.oracle.com/javase/7/docs/jre/api/management/extension/com/sun/management/OperatingSystemMXBean.html#getProcessCpuLoad())
  for more information.
* If the JVM doesn't support the required operations, the metrics above won't be reported at all.

## Dynamic Loading of Agent

1. Make sure you have the `tools.jar` available in your classpath during compilation and runtime. This JAR is usually found in the JAVA_HOME directory under the `/lib` folder for Oracle Java installations.
2. Make sure the `jvm-profiler` JAR is available during runtime. 
3. During your application boostrap process, do the following:

```scala
  val jarPath: String = s"$ABSOLUTE_PATH_TO/com.etsy.statsd-jvm-profiler-$VERSION.jar"
  val agentArgs: String = s"server=$SERVER,port=$PORT"
  attachJvmAgent(jarPath, agentArgs)

  def attachJvmAgent(profilerJarPath: String, agentArgs: String): Unit = {
    val nameOfRunningVM: String = java.lang.management.ManagementFactory.getRuntimeMXBean.getName
    val p: Integer = nameOfRunningVM.indexOf('@')
    val pid: String = nameOfRunningVM.substring(0, p)

    try {
      val vm: com.sun.tools.attach.VirtualMachine = com.sun.tools.attach.VirtualMachine.attach(pid)
      vm.loadAgent(profilerJarPath, agentArgs)
      vm.detach()
      LOGGER.info("Dynamically loaded StatsD JVM Profiler Agent...");
    } catch {
      case e: Exception => LOGGER.warn(s"Could not dynamically load StatsD JVM Profiler Agent ($profilerJarPath)", e);
    }
  }
```


## Contributing
Contributions are highly encouraged!  Check out [the contribution guidlines](https://github.com/etsy/statsd-jvm-profiler/blob/master/CONTRIBUTING.md).

Any ideas you have are welcome,  but check out [some ideas](https://github.com/etsy/statsd-jvm-profiler/wiki/Contribution-Ideas) for contributions.
