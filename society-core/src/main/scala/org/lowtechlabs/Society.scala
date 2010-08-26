package org.lowtechlabs

import org.lowtechlabs.society._
import org.linkedprocess.villein._
import proxies.{FarmProxy, JobProxy}
import org.github.scopt.OptionParser
import tools.nsc.io.File

/**
 * Gonna use this class to test out a basic villein
 *
 */
object Society {
  var hostname = "tx-dev"
  var port     = 5222
  var username = "mtodd"
  var password = "secret"
  var useSynch = false
  var interactiveMode = false
  var expression = "6 * 9" //42
  var file = ""

  val parser = new OptionParser("java -jar society.jar ") {
    opt("h","hostname", "The domain name or IP address of the XMPP server to log into.", {h: String => hostname = h})
    intOpt("port", "port", "The port of the XMPP server (usually 5222).", {p: Int => port = p})
    opt("u", "username", "The account registered with the XMPP server.", {u:String => username = u})
    opt("p", "password", "The account password.", {p: String => password = p})
    opt("e","expression", "An expression to be evaluated by the Groovy VM.\n\tEither an expression or a file should be provided.", {e: String => expression = e})
    opt("f","file", "A groovy file to evaluate.\n\tEither an expression or a file should be provided.", {f: String => file = f})
    opt("s", "synchronous", "Use synchronous villeins (usually only for testing)?", {setUseSynch(true)})
    opt("i", "interactive", "Include to enable interactive mode.", {setInteractiveMode(true)})
    opt("?","help", "Display this!", {showHelpAndQuit()})
  }

  /**
   * hostname: the domain name or IP address of the XMPP server to log into.
   * port: the port of the XMPP server (usually 5222).
   * username: the account registered with the XMPP server.
   * password: the account password.
   *
   * 
   */
  def main(args: Array[String]) {
    try {
      if(!parser.parse(args)) exit(1)
    } catch {
      case e: ArrayIndexOutOfBoundsException => {
      println("Invalid argument!")
      showHelpAndQuit()
      }
    }
    if(hostname == "" || port > 65536 || port < 1) showHelpAndQuit()
    if(password == "" && interactiveMode) {
      password = {
        print("password: ")
        val pwd = readLine
	    if (pwd == "") println("WARNING: empty password was supplied.")
        pwd
      }
    }

    if(args.length == 0) {
      print("\nNo args provided.\nWould you like to use the default values (y/n)?")
      readLine.toLowerCase match {
	                           case s: String if (s.startsWith("n")) => showHelpAndQuit()
                             case _ => println("Using default values.")}
    }
    println("----------------------------------------------")
    println("Configuration:")
    println("\thostname=" + hostname)
    println("\tport="     + port)
    println("\tusername=" + username)
    println("\tpassword=" + password)
    println("\tuseSynch=" + useSynch)
    println("----------------------------------------------")
    if (interactiveMode) {
      print("Press [Enter] to continue");
      readLine()
    } 
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

  def showHelpAndQuit(){
      print(parser.showUsage)
      exit(1)
  }
  /* For scopt recovery */
  private def setInteractiveMode(i: Boolean) = interactiveMode = i
  private def setUseSynch(u: Boolean) = useSynch = u


}
