package org.lowtechlabs

;

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
          useSynchronousVillein(farmProxies.head)
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
    submitAsynchronousJob(vmProxy)

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
    jobProxy.setExpression("1 + 2;")
    val successSubmitHandler = new SuccessSubmitHandler[JobProxy]
    val errorSubmitHandler = new ErrorSubmitHandler[JobProxy]
    vmProxy.submitJob(jobProxy, successSubmitHandler, errorSubmitHandler)
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
trait SocietyHandler[T] extends Handler[T]{
  override def handle(t: T): Unit
}

case class SuccessSpawnHandler[T] extends SocietyHandler[T] {
  override def handle(jp: T) = {
    val jobProxy = jp
  }
}

case class ErrorSpawnHandler[T] extends SocietyHandler[T] {
  override def handle(le: T) = {
    println(le)
    exit(1)
  }
}

case class SuccessSubmitHandler[T] extends SocietyHandler[T] {
  override def handle(jp: T) = {
    val jobProxy = jp
  }
}

case class ErrorSubmitHandler[T] extends SocietyHandler[T] {
  override def handle(le: T) = {
    println(le)
    exit(1)
  }
}

case class SuccessTerminateHandler[T] extends SocietyHandler[T] {
  override def handle(obj: T) = {
    println("All done. Goodbye!")
    exit()
  }
}

case class ErrorTerminateHandler[T] extends SocietyHandler[T] {
  override def handle(le: T) = {
    println(le)
    exit(1)
  }
}