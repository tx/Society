package org.lowtechlabs.society

import proxies.{FarmProxy, JobProxy, VmProxy}
import org.linkedprocess.villein._
import org.linkedprocess.LinkedProcess._
import org.linkedprocess.LopError


/**
* Handler.scala
* Simple scala wrappers for villein handlers
* @author Morgan Todd
* @version 0.1 
*/

trait Handler[T] extends org.linkedprocess.villein.Handler[T]{
  override def handle(t: T): Unit
  def printAndExit(obj: T) = {
    println(obj)
    exit(1)
  }
}

case class SuccessSpawnHandler[T] extends Handler[T] {
  override def handle(jp: T) = {
    val jobProxy = jp
  }
}

case class ErrorSpawnHandler[T] extends Handler[T] {
  override def handle(le: T) = printAndExit(le)
}

case class SuccessSubmitHandler[T] extends Handler[T] {
  override def handle(jp: T) = {
    val jobProxy = jp
    println("Result: " +jobProxy.asInstanceOf[JobProxy].getResult)
  }
}

case class ErrorSubmitHandler[T] extends Handler[T] {
  override def handle(le: T) = printAndExit(le)  
}

case class SuccessTerminateHandler[T] extends Handler[T] {
  override def handle(obj: T) = {
    println("All done. Goodbye!")
  }
}

case class ErrorTerminateHandler[T] extends Handler[T] {
  override def handle(le: T) = printAndExit(le)  
}

