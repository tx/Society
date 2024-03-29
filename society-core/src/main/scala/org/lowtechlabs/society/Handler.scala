package org.lowtechlabs.society

import org.linkedprocess.villein._
import proxies.{VmProxy, JobProxy}
import org.linkedprocess.LopError

/**
* Handler.scala
* Simple scala wrappers for villein handlers
* @author Morgan Todd
* @version 0.1 
*/

abstract class Handler[T] extends org.linkedprocess.villein.Handler[T]{
  override def handle(t: T): Unit
  def printAndExit(obj: T) = {
    println(obj)
    exit(1)
  }
}

case class SuccessHandler[T] extends Handler[T] {
  override def handle(t: T) = println(t)
}

case class ErrorHandler[T] extends Handler[T] {
  override def handle(le: T) = printAndExit(le)
}
/** 
* DefaultHandlers
* Really dumb default error and success handlers.
*   
*/
object DefaultHandlers {
  implicit val jobSuccessHandler = new SuccessHandler[JobProxy]{
    override def handle(jobProxy: JobProxy) = {
      println("Result: " +jobProxy.getResult)
    }
  }
  implicit val vmSuccessHandler = new SuccessHandler[VmProxy]
  implicit val jobErrorHandler = new ErrorHandler[JobProxy]
  implicit val successHandler = new SuccessHandler[Any]
  implicit val errorHandler = new ErrorHandler[LopError]


}
