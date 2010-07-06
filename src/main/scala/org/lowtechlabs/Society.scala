package org.lowtechlabs

import org.lowtechlabs.society._
import org.linkedprocess.villein._
import proxies.{FarmProxy, JobProxy}
import org.github.scopt.OptionParser

/**
 * Gonna use this class to test out a basic villein
 *
 */
object Society {
  var hostname   = "tx-dev"
  var port     = 5222
  var username = "mtodd"
  var password = "secret"
  var useSynch = false
  var interactiveMode = false
  var expression = "6 * 9" //42

  val parser = new OptionParser("Society") {
    opt("h","hostname", "The domain name or IP address of the XMPP server to log into.", {h: String => hostname = h})
    intOpt("p", "port", "The port of the XMPP server (usually 5222)", {p: Int => port = p})
    opt("u", "username", "The account registered with the XMPP server", {u:String => username = u})
    opt("p", "password", "The account password", {p: String => password = p})
    booleanOpt("s", "synchronous", "Use synchronous villeins? (usually only for testing)", {s: Boolean => useSynch = s})
    booleanOpt("i", "interactive", "Interactive or headless mode?", {i: Boolean => interactiveMode = i})
    opt("e","expression", "An expression to be evaluated by the Groovy vm", {e: String => expression = e})

  }
  /**
   * hostname: the domain name or IP address of the XMPP server to log into.
   * port: the port of the XMPP server (usually 5222).
   * username: the account registered with the XMPP server.
   * password: the account password.
   */
  def main(args: Array[String]) {
    if(!parser.parse(args)) exit(1)
    if(hostname == "" || port > 65536 || port < 1) showHelp()
    if(password == "" && interactiveMode){
      password = {
        print("password: ")
        val pwd = readLine
	if (pwd == "") println("WARNING: empty password was supplied.")
        pwd
      }
    }
    if(args.length == 0) {
      println("No args provided. Using default values.")
    }
    println("----------------------------------------------")
    println("Configuration:")
    println("\thostname=" + hostname)
    println("\tport=" + port)
    println("\tusername=" + username)
    println("\tpassword=" + password)
    println("\tuseSynch=" + useSynch)
    println("----------------------------------------------")
    if (interactiveMode) print("Press [Enter] to continue"); readLine()
    
    val villein = new Villein(hostname, port, username, password)
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
    job.setExpression(expression)
    val agent = if(useSynch) SynchronousAgent(job, 5000L)
                else AsynchronousAgent(job, 5000L)
    agent.use(farmProxies.head, "groovy")

  }

  def showHelp(){
      print(parser.showUsage)
      exit(1)
  }
}
