package org.lowtechlabs.society

/** 
* Cloud
*      A Cloud is a workspace for {@link Processor}s to work in. It
* represents a thread in single processor systems and a Farm in
* linked process terms.
*   
*/

import org.linkedprocess.villein._
import proxies.{FarmProxy, JobProxy, VmProxy}
import org.linkedprocess.LopError

object Cloud {
  implicit def farmProxyToCloud(farm: FarmProxy): Cloud = new Cloud(farm)
}

class Cloud(farm: FarmProxy){
  def spawn(farmType: String)(implicit successHandler: SuccessHandler[VmProxy], errorHandler: ErrorHandler[LopError]): Unit = farm.spawnVm(farmType, successHandler, errorHandler)
}
