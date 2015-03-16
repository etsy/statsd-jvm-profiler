package com.etsy.cascading.flow

import java.util.Random

import cascading.flow.{Flow, FlowListener, FlowStep}
import org.apache.hadoop.mapred.JobConf

import scala.util._
import scala.collection.JavaConversions._

/**
 * Flow listener for setting up JobConf for the StatsD profiler
 */
class StatsDProfilerFlowListener extends FlowListener {
  override def onStarting(flow: Flow[_]): Unit = {
    val statsdHost = "statsd"
    val statsdPort = 8125
    val userName = System.getProperty("user.name")
    val flowId = flow.getID
    val jobName = flow.getName

    flow.getFlowSteps.toList foreach { fs: FlowStep[_] =>
      val stepNum = fs.getStepNum.toString
      if (fs.getConfig.isInstanceOf[JobConf]) {
        val conf = fs.getConfig.asInstanceOf[JobConf]
        val javaAgent = String.format("'-javaagent:/usr/etsy/statsd-jvm-profiler/statsd-jvm-profiler.jar=server=%s,port=%s,prefix=bigdata.profiler.%s.%s.%s.%s,packageWhitelist=com.etsy:com.twitter.scalding:cascading'",
          statsdHost, statsdPort.toString, userName, jobName, flowId, stepNum)
        val numMapTasks = conf.get("mapred.map.tasks")
        val numReduceTasks = conf.get("mapred.reduce.tasks")

        conf.setBoolean("mapred.task.profile", true)
        // If you are using YARN you can use the map/reduce specific version of this property
        // This would allow you to set different parameters if desired
        conf.set("mapred.task.profile.params", javaAgent)
        conf.set("mapred.task.profile.maps", getTaskToProfile(numMapTasks,
          String.format("statsd.profiler.map%s.task", stepNum), conf))
        conf.set("mapred.task.profile.reduces", getTaskToProfile(numReduceTasks,
          String.format("statsd.profiler.reduce%s.task", stepNum), conf))
      } else {
        // Profiling is off
      }
    }
  }

  override def onCompleted(flow: Flow[_]): Unit = ()

  override def onThrowable(flow: Flow[_], t: Throwable): Boolean = false

  override def onStopping(flow: Flow[_]): Unit = ()

  private def getTaskToProfile(numTasks: String, overrideProperty: String, conf: JobConf): String = {
    conf.get(overrideProperty) match {
      case null => Try(Integer.parseInt(numTasks)) match {
        case Success(0) => "0"
        case Success(n: Int) => new Random().nextInt(n).toString
        case _ => "0"
      }
      case n: String => n
    }
  }
}
