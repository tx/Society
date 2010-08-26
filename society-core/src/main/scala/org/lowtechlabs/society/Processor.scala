package org.lowtechlabs.society
/**
* Processor.scala
*
*    A processor is a virtual machine dedicated to performing a specific class of tasks. A
* Processor is synonymous with the Linked Process concept of a vm.
* {@see VmProxy}
*
* @author Morgan Todd
* @version 0.1 
*/
import org.linkedprocess.villein._
import proxies.{FarmProxy, JobProxy, VmProxy}
import org.linkedprocess.LopError

object Processor {
  implicit def societyVmToProcessor(vm: VmProxy): Processor = new Processor(vm)
}

class Processor(vm: VmProxy){
  def submit(job: JobProxy)(implicit successHandler: SuccessHandler[JobProxy], errorHandler: ErrorHandler[JobProxy]) = vm.submitJob(job, successHandler, errorHandler)
  def terminate()(implicit successHandler: SuccessHandler[Any], errorHandler: ErrorHandler[LopError]): Unit = vm.terminateVm(successHandler.asInstanceOf[Handler[java.lang.Object]], errorHandler.asInstanceOf[Handler[LopError]])
}


