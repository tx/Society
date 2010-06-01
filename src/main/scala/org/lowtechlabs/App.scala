package org.lowtechlabs

;

import org.linkedprocess.villein._
import org.linkedprocess.LinkedProcess._
import patterns.{TimeoutException, SynchronousPattern}
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
    val villein = new Villein(server, port, username, password)
    villein.createCloudFromRoster
    for(i <- 0 until 60){
      val farmProxies: List[FarmProxy] = villein.getCloudProxy.getFarmProxies.toArray.toList.asInstanceOf[List[FarmProxy]]
      if(farmProxies.length > 0) {
        try {
          spawnSynchronousVillein(farmProxies.head)
        } catch {
            case e: TimeoutException => {
              println("It took longer than 5 seconds to execute this job.")
              exit(1)
            }
        }
      } else {
        println("Try #" +i+ "; no farms found!")
      }
      Thread.sleep(1L)
    }
  }
  /*
    // Synchronous Villein Java implementation
    JobProxy jobProxy = new JobProxy();
    jobProxy.setExpression("1 + 2;");
    try {
      ResultHolder<JobProxy> result = SynchronousPattern.submitJob(vmProxy, jobProxy, 5000);
      if(result.wasSuccessful()) {
        System.out.println(result.getSuccess().getResult());
      } else {
        System.out.println(result.getLopError());
        System.exit(1);
      }
    } catch(TimeoutException e) {
      System.out.println("It took longer than 5 seconds to execute this job.");
      System.exit(1);
    }

   */
  def spawnSynchronousVillein(farmProxy: FarmProxy) = {
    val startTime = System.currentTimeMillis
    val farmResult = SynchronousPattern.spawnVm(farmProxy, "groovy", 10000)
    if (farmResult.wasSuccessful) {
      val vmProxy = farmResult.getSuccess
      val jobProxy = new JobProxy
      jobProxy.setExpression("1 + 2;")
      val jobResult = SynchronousPattern.submitJob(vmProxy, jobProxy, 5000)
      if (jobResult.wasSuccessful) {
        println(jobResult.getSuccess.getResult)
        SynchronousPattern.terminateVm(vmProxy, (System.currentTimeMillis - startTime))
      } else {
        println(jobResult.getLopError())
        exit()
      }
    } else {
      println(farmResult.getLopError())
      exit()
    }
  }
}
