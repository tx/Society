package org.lowtechlabs

;

import org.linkedprocess.villein._
import org.linkedprocess.LinkedProcess._
import patterns.SynchronousPattern
import proxies.{FarmProxy, JobProxy, VmProxy}

/**
 * Gonna use this class to test out a basic villein
 *
 */
object App {
  /**
   * server: the domain name or IP address of the XMPP server to log into.
   * port: the port of the XMPP server (usually 5222).
   * username: the account registered with the XMPP server.
   * password: the account password.
   */
  val server = "tx-dev"
  val port = 5222
  val username = "mtodd"
  val password = "secret"

  def main(args: Array[String]) {
    val villein = new Villein(server, port, username, password);
    villein.createCloudFromRoster
    for(i <- 0 until 3600){
      val farmProxies = villein.getCloudProxy.getFarmProxies.toArray.toList
      if(farmProxies.length > 0) {
        val farmResult = SynchronousPattern.spawnVm(farmProxies.head.asInstanceOf[FarmProxy], "groovy", 10000)
        if (farmResult.wasSuccessful) {
          val vmProxy = farmResult.getSuccess
          val jobProxy = new JobProxy
          jobProxy.setExpression("1 + 2;")
          val jobResult = SynchronousPattern.submitJob(vmProxy, jobProxy, 5000)
          if (jobResult.wasSuccessful) println(jobResult.getSuccess.getResult)
          else {println(jobResult.getLopError()); exit()}
          SynchronousPattern.terminateVm(vmProxy, -1)
        } else {
          println(farmResult.getLopError())
          exit()
        }
      } else {
        println("Try #" +i+ "; no farms found!")
      }
      Thread.sleep(1L)
    }
  }
}
