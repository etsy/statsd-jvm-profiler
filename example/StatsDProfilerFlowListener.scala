package com.etsy.cascading.flow

import java.util.Properties

import cascading.flow.{Flow, FlowListener, FlowStep}
import org.apache.hadoop.mapred.JobConf

import scala.collection.JavaConversions._

/**
 * Flow listener for setting up JobConf to enable statsd-jvm-profiler
 */
class StatsDProfilerFlowListener extends FlowListener {
  val baseParamsFormat = "-javaagent:%s=server=%s,port=%s,prefix=bigdata.profiler.%s.%s.%s.%%s.%%s,packageWhitelist=%s,packageBlacklist=%s,username=%s,password=%s,database=%s,reporter=%s"

  override def onStarting(flow: Flow[_]): Unit = {
    val profilerProps = loadProperties("statsd-jvm-profiler.properties")

    val jarPath = profilerProps.getProperty("jar.location")
    val host = profilerProps.getProperty("host")
    val port = profilerProps.getProperty("port")
    val userName = System.getProperty("user.name")
    val flowId = flow.getID
    val jobName = flow.getName.replace(".", "-")
    val reporter = profilerProps.getProperty("reporter")
    val packageBlacklist = profilerProps.getProperty("package.blacklist")
    val packageWhiteList = profilerProps.getProperty("package.whitelist")
    val influxdbUser = profilerProps.getProperty("influxdb.user")
    val influxdbPassword = profilerProps.getProperty("influxdb.password")
    val influxdbDatabase = profilerProps.getProperty("influxdb.database")
    val dashboardUrl = profilerProps.getProperty("dashboard.url")

    val baseParams = baseParamsFormat.format(jarPath, host, port, userName, jobName, flowId, packageWhiteList, packageBlacklist, influxdbUser, influxdbPassword, influxdbDatabase, reporter)

    flow.getFlowSteps.toList foreach { fs: FlowStep[_] =>
      val stepNum = fs.getStepNum.toString
      val conf = fs.getConfig.asInstanceOf[JobConf]
      val numReduceTasks = conf.get("mapreduce.job.reduces")

      conf.setBoolean("mapreduce.task.profile", true)
      conf.set("mapreduce.task.profile.map.params", baseParams.format(stepNum, "map"))
      conf.set("mapreduce.task.profile.reduce.params", baseParams.format(stepNum, "reduce"))
      // In newer versions of Cascading/Scalding it seems to no longer be possible to retrieve the correct
      // number of map or reduce tasks from the flow.
      // As such we have to profile a predetermined task every time, rather than picking a random one
      conf.set("mapreduce.task.profile.maps", getTaskToProfile(stepNum, "map", conf))
      conf.set("mapreduce.task.profile.reduces", getTaskToProfile(stepNum, "reduce", conf))

      // If you use https://github.com/etsy/Sahale this will cause links to the profiler dashboard to appear in Sahale
      // for jobs that are profiled
      val additionalLinksOrig = conf.get("sahale.additional.links", "")
      val additionalLinks = numReduceTasks.toInt match {
        case i: Int if i <= 0 => "%s;Profiler Dashboard - Map|%s".format(additionalLinksOrig, dashboardUrl.format("map"))
        case _ => "%s;Profiler Dashboard - Map|%s;Profiler Dashboard - Reduce|%s".format(additionalLinksOrig, dashboardUrl.format("map"), dashboardUrl.format("reduce"))
      }
      conf.set("sahale.additional.links", additionalLinks)
    }
  }

  private def getTaskToProfile(stage: String, phase: String, conf: JobConf): String = {
    val prop = "profiler.stage%s.%s".format(stage, phase)
    conf.get(prop, "0")
  }

  private def loadProperties(resourceName: String): Properties = {
    val props = new Properties()
    props.load(Thread.currentThread.getContextClassLoader.getResourceAsStream(resourceName))

    props
  }

  override def onThrowable(flow: Flow[_], t: Throwable): Boolean = false

  override def onStopping(flow: Flow[_]): Unit = ()

  override def onCompleted(flow: Flow[_]): Unit = ()
}
