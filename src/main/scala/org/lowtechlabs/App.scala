package org.lowtechlabs

import org.lowtechlabs.society._
import org.linkedprocess.villein._
import org.linkedprocess.LinkedProcess._
import patterns.{TimeoutException, SynchronousPattern}
import proxies.{FarmProxy, JobProxy, VmProxy}
import org.linkedprocess.LopError

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
    val port     = if (args.length > 1) Long.parseLong(args(1))   else 5222
    val username = if (args.length > 2) args(2)                   else "mtodd"
    val password = if (args.length > 3) args(3) else {
      val pwd = readLine
      if (!pwd.isEmpty) pwd
      else "secret"
    }


    val villein = new Villein(server, port, username, password)
    villein.createCloudFromRoster
    print("\nTrying to get a farm proxy.")
    while(villein.getCloudProxy.getFarmProxies.size == 0){
      print(".")
      Thread.sleep(1000L)
    }
    val farmProxies: List[FarmProxy] = villein.getCloudProxy.getFarmProxies.toArray.toList.asInstanceOf[List[FarmProxy]]
    useAsynchronousVillein(farmProxies.head)
    
    try {
      useSynchronousVillein(farmProxies.head)
    } catch {
        case e: TimeoutException => {
          println("It took longer than 5 seconds to execute this job.")
          exit(1)
        }
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
  def useSynchronousVillein(farmProxy: FarmProxy) = {
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

  def useAsynchronousVillein(farmProxy: FarmProxy) = {
    val vmProxy: VmProxy = spawnAsynchronousVillein(farmProxy).head
    println("We've got a vm, let's submit a job.")
    submitAsynchronousJob(vmProxy)
    //Give it some time to work before killing it
    Thread.sleep(5000L)
    println("!!! DONE !!!")
    terminateAsynchronousVillein(vmProxy)

  }
  /*
  VmProxy vmProxy;
  Handler<VmProxy> successSpawnHandler = new Handler<VmProxy>() {
    public void handle(VmProxy vmProxy) {
      vmProxy = vmProxy;
    }
  };
  Handler<LopError> errorSpawnHandler = new Handler<LopError>() {
    public void handle(LopError lopError) {
      System.out.println(lopError);
      System.exit(1);
    }
  };
  farmProxy.spawnVm("groovy", successSpawnHandler, errorSpawnHandler);
   */

  def spawnAsynchronousVillein(farmProxy: FarmProxy): List[VmProxy] ={
    val successSpawnHandler = new SuccessSpawnHandler[VmProxy]
    val errorSpawnHandler = new ErrorSpawnHandler[LopError]
    farmProxy.spawnVm("groovy", successSpawnHandler, errorSpawnHandler)
    print("\nSpawned vm, waiting for it to come online")
    while(farmProxy.getVmProxies.toArray.size == 0){
      Thread.sleep(1000L)
      print(".")
    }
    farmProxy.getVmProxies.toArray.toList.asInstanceOf[List[VmProxy]]
  }

/*
  JobProxy jobProxy = new JobProxy();
  jobProxy.setExpression("1 + 2;");
  Handler<JobProxy> successSubmitHandler = new Handler<JobProxy>() {
    public void handle(JobProxy jobProxy) {
      jobProxy = jobProxy;
    }
  };
  Handler<LopError> errorSubmitHandler = new Handler<LopError>() {
    public void handle(LopError lopError) {
      System.out.println(lopError);
      System.exit(1);
    }
  };
  vmProxy.submitJob(jobProxy, successSubmitHandler, errorSubmitHandler);
 */

  def submitAsynchronousJob(vmProxy: VmProxy) = {
    val jobProxy = new JobProxy
    println("launching job")
    jobProxy.setExpression("2 + 2;")
    val successSubmitHandler = new SuccessSubmitHandler[JobProxy]
    val errorSubmitHandler = new ErrorSubmitHandler[JobProxy]
    vmProxy.submitJob(jobProxy, successSubmitHandler, errorSubmitHandler)
    println("Job launched, waiting for result.")
  }
  /*
    Handler<Object> successTerminateHandler = new Handler<Object>() {
      public void handle(Object object) {
        System.out.println("All done. Goodbye.");
        System.exit(0);
      }
    };
    Handler<LopError> errorTerminateHandler = new Handler<LopError>() {
      public void handle(LopError lopError) {
        System.out.println(lopError);
        System.exit(1);
      }
    };
    vmProxy.terminateVm(successTerminateHandler, errorTerminateHandler);
   */
  def terminateAsynchronousVillein(vmProxy: VmProxy) = {
    val successTerminateHandler = new SuccessTerminateHandler[java.lang.Object]
    val errorTerminateHandler = new ErrorTerminateHandler[LopError]
    vmProxy.terminateVm(successTerminateHandler, errorTerminateHandler)
  }
}
