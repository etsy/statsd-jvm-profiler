package com.etsy.statsd.profiler.profilers;

import com.google.common.collect.ImmutableMap;

import com.etsy.statsd.profiler.Arguments;
import com.etsy.statsd.profiler.Profiler;
import com.etsy.statsd.profiler.reporter.Reporter;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * This profiler retrieves CPU values for the JVM and System from the "OperatingSystem" JMX Bean.
 * <p>
 * This profiler relies on a JMX bean that might not be available in all JVM implementations.
 * We know for sure it's available in Sun/Oracle's JRE 7+, but there are no guarantees it
 * will remain there for the foreseeable future.
 *
 * @see <a href="http://stackoverflow.com/questions/3044841/cpu-usage-mbean-on-sun-jvm">StackOverflow post</a>
 *
 * @author Alejandro Rivera
 */
public class CPULoadProfiler extends Profiler {

  public static final long PERIOD = 10;
  private static final Map<String, String> ATTRIBUTES_MAP = ImmutableMap.of("ProcessCpuLoad", "cpu.jvm",
                                                                            "SystemCpuLoad",  "cpu.system");

  private AttributeList list;

  public CPULoadProfiler(Reporter reporter, Arguments arguments) {
    super(reporter, arguments);
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName os = ObjectName.getInstance("java.lang:type=OperatingSystem");
      list = mbs.getAttributes(os, ATTRIBUTES_MAP.keySet().toArray(new String[ATTRIBUTES_MAP.size()]));
    } catch (InstanceNotFoundException | ReflectionException | MalformedObjectNameException e) {
      list = null;
    }

  }

  /**
   * Profile memory usage and GC statistics
   */
  @Override
  public void profile() {
    recordStats();
  }

  @Override
  public void flushData() {
    recordStats();
  }

  @Override
  public long getPeriod() {
    return PERIOD;
  }

  @Override
  public TimeUnit getTimeUnit() {
    return TimeUnit.SECONDS;
  }

  @Override
  protected void handleArguments(Arguments arguments) { /* No arguments needed */ }

  /**
   * Records all memory statistics
   */
  private void recordStats() {
    if (list == null) {
      return;
    }

    Attribute att;
    Double value;
    String metric;
    for (Object o : list) {
      att = (Attribute) o;
      value = (Double) att.getValue();

      if (value == null || value == -1.0) {
        continue;
      }

      metric = ATTRIBUTES_MAP.get(att.getName());
      if (metric == null) {
        continue;
      }

      value = ((int) (value * 1000)) / 10.0d; // 0-100 with 1-decimal precision
      recordGaugeValue(metric, value);
    }
  }
}
