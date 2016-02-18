package com.etsy.statsd.profiler.profilers;

import com.etsy.statsd.profiler.Arguments;
import com.etsy.statsd.profiler.Profiler;
import com.etsy.statsd.profiler.reporter.Reporter;

import java.lang.management.ManagementFactory;
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
public class JVMCPUProfiler extends Profiler {
  public static final long PERIOD = 10;
  public static final double INVALID_VALUE = Double.NaN;
  private final MBeanServer mbs;

  public JVMCPUProfiler(Reporter reporter, Arguments arguments) {
    super(reporter, arguments);
    mbs = ManagementFactory.getPlatformMBeanServer();
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
    ObjectName os = getOperatingsystemObject();
    if (os != null) {
      recordStat(os, "ProcessCpuLoad", "cpu.jvm");
      recordStat(os, "SystemCpuLoad", "cpu.system");
    }
  }

  private void recordStat(ObjectName os, String attribute, String metricKey) {
    double value = getValue(mbs, os, attribute);
    if (value != INVALID_VALUE) {
      recordGaugeValue(metricKey, value);
    }
  }

  private ObjectName getOperatingsystemObject() {
    try {
      return ObjectName.getInstance("java.lang:type=OperatingSystem");
    } catch (MalformedObjectNameException e) {
      return null;
    }
  }

  public static double getValue(MBeanServer mbs, ObjectName name, String attribute) {

    AttributeList list;
    try {
      list = mbs.getAttributes(name, new String[]{attribute});
    } catch (InstanceNotFoundException e) {
      return INVALID_VALUE;
    } catch (ReflectionException e) {
      return INVALID_VALUE;
    }

    if (list.isEmpty()) {
      return INVALID_VALUE;
    }

    Attribute att = (Attribute) list.get(0);
    Double value = (Double) att.getValue();

    if (value == -1.0) {
      return INVALID_VALUE;
    }

    return ((int) (value * 1000)) / 10.0d; // 0-100 with 1-decimal precision
  }
}
