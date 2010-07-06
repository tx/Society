/**
* Agents.scala
* 
* @author Morgan Todd
* @version 0.1 
*/
package org.lowtechlabs.society

import org.linkedprocess.villein._
import proxies.{FarmProxy, JobProxy, VmProxy}
import patterns.{TimeoutException, SynchronousPattern}
import org.linkedprocess.LopError


/** 
* Agent
* Basic agent interface. All that should be required is to provide a job
* to work, a farm to work on and a the type of farm.
* i.e.
*   //Create a groovy vm and eval an expression
*   val job = new JobProxy
*   job.setExpression("1+2")
*   val agent = Agent(job)
*   //snip getting a farm
*   val farm = ...
*   agent.use(farm, "groovy") //eventually prints 3
*   
*/
abstract class Agent(job: JobProxy) {
  def use(farm: FarmProxy, farmType: String)
}

/** 
* SynchronousAgent
* Simple synchronous agent. This barebone implementation is only included
* for testing purposes.
*   
*/

case class SynchronousAgent(job: JobProxy, timeout: Long) extends Agent(job){
//  def this(job: JobProxy) = this(job, 5000L)

  override def use(farm: FarmProxy, farmType: String) = {
    println("Using synchronous villein.")
    val startTime = System.currentTimeMillis
    val farmResult = SynchronousPattern.spawnVm(farm, farmType, timeout)
    if (farmResult.wasSuccessful) {
      val vm = farmResult.getSuccess
      println("Submitting job to " +farmType+ "vm:\n{" +job.getExpression+ "}")
      val jobResult = SynchronousPattern.submitJob(vm, job, timeout)
      if (jobResult.wasSuccessful) {
        println("Result: " +jobResult.getSuccess.getResult)
        println("Terminating vm.")
        try{
          SynchronousPattern.terminateVm(vm, (System.currentTimeMillis - startTime))
        } catch {
          case e: TimeoutException => exit(1)
        }
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

/** 
* Asynchronous
* This trait adds support for spawning and terminating asynchronous
* agents.
*   
*/
trait Asynchronous {
/** 
  * terminate
  * Terminate the vm.
  */ 
  def terminate(vm: VmProxy) = vm.terminateVm(new SuccessTerminateHandler[java.lang.Object], 
					      new ErrorTerminateHandler[LopError])
/** 
  * spawn
  * Continually try to get a access to a farm of the specified type. This will
  * check twice a second to see if any farms are available.
  */ 
  def spawn(farm: FarmProxy, farmType: String): List[VmProxy] ={
    farm.spawnVm(farmType, 
		 new SuccessSpawnHandler[VmProxy], 
		 new ErrorSpawnHandler[LopError])
    print("\nSpawned vm, waiting for it to come online")
    //TODO: This is kind of an ugly way to do this; also, magic number, wtf?
    while(farm.getVmProxies.toArray.size == 0){
      Thread.sleep(500L)
      print(".")
    }
    farm.getVmProxies.toArray.toList.asInstanceOf[List[VmProxy]]
  }
  def submitJob(job: JobProxy, vm: VmProxy) = {
    vm.submitJob(job, new SuccessSubmitHandler[JobProxy], new ErrorSubmitHandler[JobProxy])
    println("Job launched, waiting for result.")
  }

}


case class AsynchronousAgent(job: JobProxy, timeout: Long) extends Agent(job) with Asynchronous{
  //def this(job: JobProxy) = this(job, 5000L)
  override def use(farm: FarmProxy, farmType: String) = {
    println("Using asynchronous villein.")
    /* As opposed to just grabbing the first vm this should
      provide support for multiple vms and strategies such as
      scatter gather.
     */
    //TODO Correctly handle a list of VMs
    val vm: VmProxy = spawn(farm, farmType).head
    println("Submitting job to " +farmType+ "vm:\n{" +job.getExpression+ "}")
    submitJob(job, vm)
    //Give it some time to work before killing it
    Thread.sleep(timeout)
    println("Terminating vm.")
    terminate(vm)
  }
}
