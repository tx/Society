package org.lowtechlabs

import org.lowtechlabs.society._
import org.linkedprocess.villein._
import proxies.{FarmProxy, JobProxy}

/**
 * Gonna use this class to test out a basic villein
 *
 */
object App {
  def main(args: Array[String]) {
    /**
     * server: the domain name or IP address of the XMPP server to log into.
     * port: the port of the XMPP server (usually 5222).
     * username: the account registered with the XMPP server.
     * password: the account password.
     */

    //Get args in a simple way
    val server   = if (args.length > 0) args(0)                   else "tx-dev"
    val port     = if (args.length > 1) Integer.parseInt(args(1)) else 5222
    val username = if (args.length > 2) args(2)                   else "mtodd"
    val password = if (args.length > 3) args(3) else {
      print("password: ")
      val pwd = readLine
      if (!pwd.isEmpty) pwd
      else "secret"
    }
    val useSynch = if(args.length > 4) (args(4) == "synch") else false
    println("Args: ")
    args.foreach(arg => println("\t"+arg))
    println("Using:")
    println("\tserver=" + server)
    println("\tport=" + port)
    println("\tusername=" + username)
    println("\tpassword=" + password)
    println("\tuseSynch=" + useSynch)

    val villein = new Villein(server, port, username, password)
    villein.createCloudFromRoster
    print("\nTrying to get a farm proxy.")
    while(villein.getCloudProxy.getFarmProxies.size == 0){
      print(".")
      Thread.sleep(1000L)
    }
    //get some farms
    val farmProxies: List[FarmProxy] = villein.getCloudProxy.getFarmProxies.toArray.toList.asInstanceOf[List[FarmProxy]]
    //Construct our job
    val job = new JobProxy
    job.setExpression("3 + 5")
    val agent = if(useSynch) SynchronousAgent(job, 5000L)
                else AsynchronousAgent(job, 5000L)
    agent.use(farmProxies.head, "groovy")

  }
}